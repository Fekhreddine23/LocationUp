package com.mobility.mobility_backend.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobility.mobility_backend.dto.identity.IdentitySessionResponse;
import com.mobility.mobility_backend.dto.identity.IdentityStatusResponse;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.entity.UserIdentityVerification;
import com.mobility.mobility_backend.entity.UserIdentityVerification.VerificationStatus;
import com.mobility.mobility_backend.repository.UserIdentityVerificationRepository;
import com.mobility.mobility_backend.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.identity.VerificationSession;
import com.stripe.param.identity.VerificationSessionCreateParams;

@Service
@Transactional
public class IdentityVerificationService {

	private final UserRepository userRepository;
	private final UserIdentityVerificationRepository verificationRepository;

	@Value("${stripe.secret.key:}")
	private String stripeSecretKey;

	@Value("${app.identity.return-url:http://localhost:4200/profile/identity}")
	private String defaultReturnUrl;

	public IdentityVerificationService(UserRepository userRepository,
			UserIdentityVerificationRepository verificationRepository) {
		this.userRepository = userRepository;
		this.verificationRepository = verificationRepository;
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
