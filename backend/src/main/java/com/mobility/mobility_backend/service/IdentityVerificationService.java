package com.mobility.mobility_backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobility.mobility_backend.dto.identity.IdentityDocumentDTO;
import com.mobility.mobility_backend.dto.identity.IdentitySessionResponse;
import com.mobility.mobility_backend.dto.identity.IdentityStatusResponse;
import com.mobility.mobility_backend.dto.identity.IdentityVerificationStatsDTO;
import com.mobility.mobility_backend.dto.identity.IdentityVerificationRecordDTO;
import com.mobility.mobility_backend.dto.socket.NotificationCategory;
import com.mobility.mobility_backend.dto.socket.NotificationMessage;
import com.mobility.mobility_backend.dto.socket.NotificationSeverity;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.entity.UserIdentityDocument;
import com.mobility.mobility_backend.entity.UserIdentityVerification;
import com.mobility.mobility_backend.entity.UserIdentityVerification.VerificationStatus;
import com.mobility.mobility_backend.repository.UserIdentityDocumentRepository;
import com.mobility.mobility_backend.repository.UserIdentityVerificationRepository;
import com.mobility.mobility_backend.repository.UserRepository;
import com.mobility.mobility_backend.service.notification.NotificationService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.identity.VerificationReport;
import com.stripe.model.identity.VerificationSession;
import com.stripe.param.identity.VerificationSessionCreateParams;

@Service
@Transactional
public class IdentityVerificationService {

	private final UserRepository userRepository;
	private final UserIdentityVerificationRepository verificationRepository;
	private final UserIdentityDocumentRepository documentRepository;
	private final NotificationService notificationService;

	@Value("${stripe.secret.key:}")
	private String stripeSecretKey;

	@Value("${app.identity.return-url:http://localhost:4200/profile/identity}")
	private String defaultReturnUrl;

	public IdentityVerificationService(UserRepository userRepository,
			UserIdentityVerificationRepository verificationRepository,
			UserIdentityDocumentRepository documentRepository,
			NotificationService notificationService) {
		this.userRepository = userRepository;
		this.verificationRepository = verificationRepository;
		this.documentRepository = documentRepository;
		this.notificationService = notificationService;
	}

	public IdentitySessionResponse startVerification(Integer userId, Integer reservationId, String documentType,
			String returnUrl) throws StripeException {
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
		if (reservationId != null) {
			builder.putMetadata("reservationId", reservationId.toString());
		}
		if (documentType != null && !documentType.isBlank()) {
			builder.putMetadata("documentType", documentType);
		}
		if (user.getEmail() != null) {
			builder.putMetadata("email", user.getEmail());
		}

		VerificationSessionCreateParams params = builder.build();

		VerificationSession session = VerificationSession.create(params);

		UserIdentityVerification verification = new UserIdentityVerification();
		verification.setUser(user);
		verification.setStripeSessionId(session.getId());
		verification.setReservationId(reservationId);
		verification.setDocumentType(documentType);
		verification.setStatus(mapStripeStatus(session.getStatus()));
		verification.setReason(null);
		verification.setLastCheckedAt(LocalDateTime.now());
		verificationRepository.save(verification);

		return new IdentitySessionResponse(session.getId(), session.getClientSecret(), session.getStatus());
	}

	public IdentityStatusResponse getStatus(Integer userId) {
		IdentityStatusResponse response = verificationRepository.findTopByUser_IdOrderByCreatedAtDesc(userId)
				.map(this::toStatusResponse)
				.orElseGet(() -> new IdentityStatusResponse("NONE", false, null, null));
		if (userId != null) {
			response.setDocuments(mapDocumentsForUser(userId));
		}
		return response;
	}

  public String getLatestStatusLabel(Integer userId) {
    IdentityStatusResponse response = getStatus(userId);
    return response != null ? response.getStatus() : null;
  }

	public Map<Integer, IdentityStatusResponse> getLatestStatusForUsers(Collection<Integer> userIds) {
		Map<Integer, IdentityStatusResponse> result = new HashMap<>();
		if (userIds == null || userIds.isEmpty()) {
			return result;
		}
		List<Integer> distinctIds = new ArrayList<>(new HashSet<>(userIds));
		List<UserIdentityVerification> history = verificationRepository
				.findByUser_IdInOrderByUpdatedAtDesc(distinctIds);
		for (UserIdentityVerification record : history) {
			if (record.getUser() == null || record.getUser().getId() == null) {
				continue;
			}
			Integer userId = record.getUser().getId();
			result.putIfAbsent(userId, toStatusResponse(record));
			if (result.size() == distinctIds.size()) {
				break;
			}
		}
		// ensure users without records are present
		for (Integer id : distinctIds) {
			result.putIfAbsent(id, new IdentityStatusResponse("NONE", false, null, null));
		}
		return result;
	}

	public boolean isIdentityVerified(Integer userId) {
		if (userId == null) {
			return false;
		}
		IdentityStatusResponse status = getStatus(userId);
		return status != null && status.isVerified();
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
		applyIdentityMetadata(verification, userMessage);
		notificationService.sendNotification(userMessage);

		if (verification.getStatus() == VerificationStatus.VERIFIED
				|| verification.getStatus() == VerificationStatus.REQUIRES_INPUT
				|| verification.getStatus() == VerificationStatus.REJECTED) {
			NotificationMessage adminMessage = new NotificationMessage();
			adminMessage.setCategory(NotificationCategory.SYSTEM_ALERT);
			adminMessage.setSeverity(mapSeverity(verification.getStatus()));
			adminMessage.setTitle("Mise à jour identité");
			adminMessage.setMessage("Utilisateur #" + userId + " : " + buildAdminMessage(verification.getStatus()));
			adminMessage.setRecipient("all");
			adminMessage.getMetadata().put("systemEvent", "IDENTITY_" + verification.getStatus().name());
			adminMessage.getMetadata().put("identityStatus", verification.getStatus().name());
			adminMessage.getMetadata().put("identityUserId", userId);
			adminMessage.getMetadata().put("targetRole", "ROLE_ADMIN");
			applyIdentityMetadata(verification, adminMessage);
			notificationService.sendNotification(adminMessage);
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

	private void applyIdentityMetadata(UserIdentityVerification verification, NotificationMessage message) {
		if (verification.getStripeSessionId() != null) {
			message.getMetadata().put("identitySessionId", verification.getStripeSessionId());
		}
		if (verification.getReservationId() != null) {
			message.getMetadata().put("identityReservationId", verification.getReservationId().toString());
		}
		if (verification.getDocumentType() != null) {
			message.getMetadata().put("identityDocumentType", verification.getDocumentType());
		}
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
		applyMetadataFromSession(verification, session);
		verification.setLastCheckedAt(LocalDateTime.now());
		verificationRepository.save(verification);
		syncDocumentsFromSession(verification, session);
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
			applyMetadataFromSession(verification, session);
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

	private IdentityStatusResponse toStatusResponse(UserIdentityVerification verification) {
		if (verification == null) {
			return new IdentityStatusResponse("NONE", false, null, null);
		}
		return new IdentityStatusResponse(verification.getStatus().name(),
				verification.getStatus() == VerificationStatus.VERIFIED,
				verification.getReason(), verification.getUpdatedAt());
	}

	private void applyMetadataFromSession(UserIdentityVerification verification, VerificationSession session) {
		if (verification == null || session == null || session.getMetadata() == null) {
			return;
		}
		String reservationIdValue = session.getMetadata().get("reservationId");
		if (reservationIdValue != null && verification.getReservationId() == null) {
			try {
				verification.setReservationId(Integer.valueOf(reservationIdValue));
			} catch (NumberFormatException ignored) {
			}
		}
		String documentType = session.getMetadata().get("documentType");
		if (documentType != null && !documentType.isBlank()) {
			verification.setDocumentType(documentType);
		}
	}

	private void syncDocumentsFromSession(UserIdentityVerification verification, VerificationSession session) {
		if (verification == null) {
			return;
		}
		documentRepository.deleteByVerification(verification);
		if (session == null) {
			return;
		}
		String reportId = session.getLastVerificationReport();
		if (reportId == null || reportId.isBlank()) {
			return;
		}
		if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
			return;
		}
		Stripe.apiKey = stripeSecretKey;
		VerificationReport report;
		try {
			report = VerificationReport.retrieve(reportId);
		} catch (StripeException ex) {
			throw new IllegalStateException("Impossible de récupérer le rapport Stripe " + reportId, ex);
		}
		if (report.getDocument() == null) {
			return;
		}
		UserIdentityDocument document = new UserIdentityDocument();
		document.setUser(verification.getUser());
		document.setVerification(verification);
		document.setStripeReportId(report.getId());
		document.setDocumentType(report.getDocument().getType());
		document.setStatus(report.getDocument().getStatus());
		document.setIssuingCountry(report.getDocument().getIssuingCountry());
		document.setExpirationDate(toLocalDate(report.getDocument().getExpirationDate()));
		if (report.getDocument().getFiles() != null) {
			document.setFileIds(new ArrayList<>(report.getDocument().getFiles()));
		}
		documentRepository.save(document);
	}

	private LocalDate toLocalDate(VerificationReport.Document.ExpirationDate expirationDate) {
		if (expirationDate == null || expirationDate.getYear() == null || expirationDate.getMonth() == null
				|| expirationDate.getDay() == null) {
			return null;
		}
		return LocalDate.of(expirationDate.getYear().intValue(), expirationDate.getMonth().intValue(),
				expirationDate.getDay().intValue());
	}

	private List<IdentityDocumentDTO> mapDocumentsForUser(Integer userId) {
		if (userId == null) {
			return List.of();
		}
		return documentRepository.findByUser_IdOrderByUpdatedAtDesc(userId).stream()
				.map(this::toDocumentDto)
				.collect(Collectors.toList());
	}

	private IdentityDocumentDTO toDocumentDto(UserIdentityDocument document) {
		IdentityDocumentDTO dto = new IdentityDocumentDTO();
		dto.setId(document.getId());
		dto.setStripeReportId(document.getStripeReportId());
		dto.setDocumentType(document.getDocumentType());
		dto.setStatus(document.getStatus());
		dto.setIssuingCountry(document.getIssuingCountry());
		dto.setExpirationDate(document.getExpirationDate());
		if (document.getFileIds() != null) {
			dto.setFileIds(new ArrayList<>(document.getFileIds()));
		} else {
			dto.setFileIds(new ArrayList<>());
		}
		dto.setUpdatedAt(document.getUpdatedAt());
		return dto;
	}

	public List<IdentityDocumentDTO> getDocumentsForUser(Integer userId) {
		return mapDocumentsForUser(userId);
	}

	public List<IdentityVerificationRecordDTO> getVerificationHistory(Integer userId) {
		if (userId == null) {
			return List.of();
		}
		return verificationRepository.findByUser_IdOrderByUpdatedAtDesc(userId).stream()
				.map(this::toRecordDto)
				.collect(Collectors.toList());
	}

	private IdentityVerificationRecordDTO toRecordDto(UserIdentityVerification verification) {
		IdentityVerificationRecordDTO dto = new IdentityVerificationRecordDTO();
		dto.setId(verification.getId());
		if (verification.getUser() != null) {
			dto.setUserId(verification.getUser().getId());
		}
		dto.setReservationId(verification.getReservationId());
		dto.setStatus(verification.getStatus() != null ? verification.getStatus().name() : "PENDING");
		dto.setReason(verification.getReason());
		dto.setStripeSessionId(verification.getStripeSessionId());
		dto.setDocumentType(verification.getDocumentType());
		dto.setCreatedAt(verification.getCreatedAt());
		dto.setUpdatedAt(verification.getUpdatedAt());
		return dto;
	}
}
