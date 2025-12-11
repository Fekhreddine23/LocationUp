package com.mobility.mobility_backend.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobility.mobility_backend.dto.identity.IdentitySessionResponse;
import com.mobility.mobility_backend.dto.identity.IdentityStatusResponse;
import com.mobility.mobility_backend.dto.identity.IdentityVerificationStatsDTO;
import com.mobility.mobility_backend.dto.socket.NotificationCategory;
import com.mobility.mobility_backend.dto.socket.NotificationMessage;
import com.mobility.mobility_backend.dto.socket.NotificationSeverity;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.entity.UserIdentityVerification;
import com.mobility.mobility_backend.entity.UserIdentityVerification.VerificationStatus;
import com.mobility.mobility_backend.repository.UserIdentityVerificationRepository;
import com.mobility.mobility_backend.repository.UserRepository;
import com.mobility.mobility_backend.service.notification.NotificationService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.identity.VerificationSession;
import com.stripe.param.identity.VerificationSessionCreateParams;

@Service
@Transactional
public class IdentityVerificationService {

	private final UserRepository userRepository;
	private final UserIdentityVerificationRepository verificationRepository;
	private final NotificationService notificationService;

	@Value("${stripe.secret.key:}")
	private String stripeSecretKey;

	@Value("${app.identity.return-url:http://localhost:4200/profile/identity}")
	private String defaultReturnUrl;

	public IdentityVerificationService(UserRepository userRepository,
			UserIdentityVerificationRepository verificationRepository,
			NotificationService notificationService) {
		this.userRepository = userRepository;
		this.verificationRepository = verificationRepository;
		this.notificationService = notificationService;
	}

	public IdentitySessionResponse startVerification(Integer userId, String returnUrl) throws StripeException {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

		if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
			throw new IllegalStateException("stripe.secret.key non configuré");
		}

		Stripe.apiKey = stripeSecretKey;

		String finalReturnUrl = (returnUrl == null || returnUrl.isBlank()) ? defaultReturnUrl : returnUrl;

		VerificationSessionCreateParams.Builder builder = VerificationSessionCreateParams.builder()
				.setType(VerificationSessionCreateParams.Type.DOCUMENT)
				.setReturnUrl(finalReturnUrl)
				.putMetadata("userId", userId.toString());
		if (user.getEmail() != null) {
			builder.putMetadata("email", user.getEmail());
		}

		VerificationSessionCreateParams params = builder.build();

		VerificationSession session = VerificationSession.create(params);

		UserIdentityVerification verification = new UserIdentityVerification();
		verification.setUser(user);
		verification.setStripeSessionId(session.getId());
		verification.setStatus(mapStripeStatus(session.getStatus()));
		verification.setReason(null);
		verification.setLastCheckedAt(LocalDateTime.now());
		verificationRepository.save(verification);

		return new IdentitySessionResponse(session.getId(), session.getClientSecret(), session.getStatus());
	}

  public IdentityStatusResponse getStatus(Integer userId) {
    return verificationRepository.findTopByUser_IdOrderByCreatedAtDesc(userId)
        .map(v -> new IdentityStatusResponse(v.getStatus().name(),
            v.getStatus() == VerificationStatus.VERIFIED, v.getReason(), v.getUpdatedAt()))
        .orElseGet(() -> new IdentityStatusResponse("NONE", false, null, null));
  }

  public String getLatestStatusLabel(Integer userId) {
    return verificationRepository.findTopByUser_IdOrderByCreatedAtDesc(userId)
        .map(v -> v.getStatus().name())
        .orElse(null);
  }

	public Map<Integer, String> getLatestStatusForUsers(Collection<Integer> userIds) {
		Map<Integer, String> result = new HashMap<>();
		if (userIds == null) {
			return result;
		}
    for (Integer userId : new HashSet<>(userIds)) {
      if (userId != null) {
        result.put(userId, getLatestStatusLabel(userId));
      }
    }
		return result;
	}

	public IdentityVerificationStatsDTO getGlobalStats() {
		List<UserIdentityVerification> history = verificationRepository.findAllByOrderByUpdatedAtDesc();
		Map<Integer, VerificationStatus> latest = new HashMap<>();
		for (UserIdentityVerification record : history) {
			if (record.getUser() != null && record.getUser().getId() != null) {
				latest.putIfAbsent(record.getUser().getId(), record.getStatus());
			}
		}
		IdentityVerificationStatsDTO stats = new IdentityVerificationStatsDTO();
		stats.setTotal(latest.size());
		latest.values().forEach(status -> {
			if (status == VerificationStatus.VERIFIED) {
				stats.setVerified(stats.getVerified() + 1);
			} else if (status == VerificationStatus.PROCESSING) {
				stats.setProcessing(stats.getProcessing() + 1);
			} else if (status == VerificationStatus.REQUIRES_INPUT || status == VerificationStatus.REJECTED) {
				stats.setRequiresInput(stats.getRequiresInput() + 1);
			} else {
				stats.setPending(stats.getPending() + 1);
			}
		});
		return stats;
	}

	private void notifyIdentityUpdate(UserIdentityVerification verification) {
		User user = verification.getUser();
		if (user == null || user.getId() == null) {
			return;
		}
		String userId = String.valueOf(user.getId());
		NotificationMessage userMessage = new NotificationMessage();
		userMessage.setCategory(NotificationCategory.USER_ACTION);
		userMessage.setSeverity(mapSeverity(verification.getStatus()));
		userMessage.setTitle("Mise à jour identité");
		userMessage.setMessage(buildUserMessage(verification.getStatus()));
		userMessage.setRecipient(userId);
		userMessage.getMetadata().put("entityType", "identity");
		userMessage.getMetadata().put("identityStatus", verification.getStatus().name());
		notificationService.sendNotification(userMessage);

		if (verification.getStatus() == VerificationStatus.VERIFIED
				|| verification.getStatus() == VerificationStatus.REQUIRES_INPUT
				|| verification.getStatus() == VerificationStatus.REJECTED) {
			notificationService.notifySystemEvent(
					"IDENTITY_" + verification.getStatus().name(),
					"Identité utilisateur #" + userId + " : " + buildAdminMessage(verification.getStatus()),
					NotificationSeverity.INFO);
		}
	}

	private NotificationSeverity mapSeverity(VerificationStatus status) {
		return switch (status) {
		case VERIFIED -> NotificationSeverity.SUCCESS;
		case PROCESSING -> NotificationSeverity.INFO;
		case REQUIRES_INPUT, REJECTED -> NotificationSeverity.WARNING;
		default -> NotificationSeverity.INFO;
		};
	}

	private String buildUserMessage(VerificationStatus status) {
		return switch (status) {
		case VERIFIED -> "Vos documents ont été validés.";
		case PROCESSING -> "Vos documents sont en analyse.";
		case REQUIRES_INPUT -> "Des compléments sont nécessaires pour valider vos documents.";
		case REJECTED -> "Vos documents ont été rejetés. Merci de réessayer.";
		default -> "Statut de vérification mis à jour.";
		};
	}

	private String buildAdminMessage(VerificationStatus status) {
		return switch (status) {
		case VERIFIED -> "documents validés.";
		case REQUIRES_INPUT -> "documents incomplets.";
		case REJECTED -> "documents rejetés.";
		default -> "statut mis à jour.";
		};
	}

	public void updateVerificationFromSession(VerificationSession session) {
		if (session == null) {
			return;
		}
		String sessionId = session.getId();
		UserIdentityVerification verification = verificationRepository.findByStripeSessionId(sessionId)
				.orElseGet(() -> buildFromMetadata(session));

		if (verification == null) {
			return;
		}

		verification.setStatus(mapStripeStatus(session.getStatus()));
		if (session.getLastError() != null) {
			verification.setReason(session.getLastError().getReason());
		} else {
			verification.setReason(null);
		}
		verification.setLastCheckedAt(LocalDateTime.now());
		verificationRepository.save(verification);
		notifyIdentityUpdate(verification);
	}

	private UserIdentityVerification buildFromMetadata(VerificationSession session) {
		if (session.getMetadata() == null) {
			return null;
		}
		String userIdValue = session.getMetadata().get("userId");
		if (userIdValue == null) {
			return null;
		}
		try {
			Integer userId = Integer.valueOf(userIdValue);
			User user = userRepository.findById(userId).orElse(null);
			if (user == null) {
				return null;
			}
			UserIdentityVerification verification = new UserIdentityVerification();
			verification.setUser(user);
			verification.setStripeSessionId(session.getId());
			verification.setStatus(mapStripeStatus(session.getStatus()));
			verification.setReason(null);
			verification.setLastCheckedAt(LocalDateTime.now());
			return verificationRepository.save(verification);
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	private VerificationStatus mapStripeStatus(String stripeStatus) {
		if (stripeStatus == null) {
			return VerificationStatus.PENDING;
		}
		return switch (stripeStatus.toLowerCase()) {
		case "processing" -> VerificationStatus.PROCESSING;
		case "requires_input" -> VerificationStatus.REQUIRES_INPUT;
		case "verified" -> VerificationStatus.VERIFIED;
		case "canceled" -> VerificationStatus.REJECTED;
		default -> VerificationStatus.PENDING;
		};
	}
}
