package com.mobility.mobility_backend.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.mobility.mobility_backend.dto.socket.NotificationCategory;
import com.mobility.mobility_backend.dto.socket.NotificationMessage;
import com.mobility.mobility_backend.dto.socket.NotificationSeverity;
import com.mobility.mobility_backend.entity.Offer;
import com.mobility.mobility_backend.entity.Reservation;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.service.notification.NotificationService;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class PaymentNotificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentNotificationService.class);

	private final JavaMailSender mailSender;
	private final boolean mailEnabled;
	private final String mailFrom;

	private final boolean smsEnabled;
	private final String smsFrom;
	private final String smsToFallback;
	private final NotificationService notificationStreamingService;

	public PaymentNotificationService(JavaMailSender mailSender,
			@Value("${notifications.mail.enabled:false}") boolean mailEnabled,
			@Value("${notifications.mail.from:no-reply@locationup.test}") String mailFrom,
			@Value("${notifications.sms.enabled:false}") boolean smsEnabled,
			@Value("${twilio.account-sid:}") String twilioSid,
			@Value("${twilio.auth-token:}") String twilioToken,
			@Value("${twilio.from-number:}") String smsFrom,
			@Value("${notifications.sms.test-recipient:+33123456789}") String smsToFallback,
			NotificationService notificationStreamingService) {
		this.mailSender = mailSender;
		this.mailEnabled = mailEnabled;
		this.mailFrom = mailFrom;
		this.smsEnabled = smsEnabled && !twilioSid.isBlank() && !twilioToken.isBlank() && !smsFrom.isBlank();
		this.smsFrom = smsFrom;
		this.smsToFallback = smsToFallback;
		this.notificationStreamingService = notificationStreamingService;

		if (this.smsEnabled) {
			Twilio.init(twilioSid, twilioToken);
		}
	}

	public void notifyPaymentSuccess(Reservation reservation) {
		String subject = "Paiement confirmé - Réservation #" + reservation.getReservationId();
		String body = "Bonjour "
				+ (reservation.getUser() != null ? reservation.getUser().getUsername() : "client")
				+ ",\n\nNous confirmons la réception de votre paiement pour la réservation #"
				+ reservation.getReservationId() + ".\nMerci pour votre confiance.\n";
		dispatch(reservation, subject, body);
		sendRealtimePaymentNotification(reservation, NotificationSeverity.SUCCESS, subject, body, "payment_success",
				"completed");
	}

	public void notifyPaymentFailure(Reservation reservation, String reason) {
		String subject = "Echec de paiement - Réservation #" + reservation.getReservationId();
		String body = "Bonjour "
				+ (reservation.getUser() != null ? reservation.getUser().getUsername() : "client")
				+ ",\n\nNous n'avons pas pu traiter votre paiement. Raison: " + reason
				+ ".\nMerci de vous reconnecter pour finaliser la transaction.\n";
		dispatch(reservation, subject, body);
		sendRealtimePaymentNotification(reservation, NotificationSeverity.ERROR, subject, body, "payment_failed",
				"failed");
	}

	public void notifyUpcomingPayment(Reservation reservation) {
		String subject = "Rappel paiement - Réservation #" + reservation.getReservationId();
		String body = "Bonjour "
				+ (reservation.getUser() != null ? reservation.getUser().getUsername() : "client")
				+ ",\n\nVotre réservation est planifiée le " + reservation.getReservationDate()
				+ ". Merci de finaliser le paiement si ce n'est pas déjà fait.\n";
		dispatch(reservation, subject, body);
		sendRealtimePaymentNotification(reservation, NotificationSeverity.WARNING, subject, body, "payment_pending",
				"pending");
	}

	public void notifyPaymentExpired(Reservation reservation) {
		String subject = "Paiement expiré - Réservation #" + reservation.getReservationId();
		String body = "Bonjour "
				+ (reservation.getUser() != null ? reservation.getUser().getUsername() : "client")
				+ ",\n\nVotre session de paiement a expiré. Merci de relancer le paiement pour confirmer la réservation #"
				+ reservation.getReservationId() + ".\n";
		dispatch(reservation, subject, body);
		sendRealtimePaymentNotification(reservation, NotificationSeverity.WARNING, subject, body, "payment_expired",
				"expired");
	}

	public void notifyPaymentRefunded(Reservation reservation, String reason) {
		String subject = "Paiement remboursé - Réservation #" + reservation.getReservationId();
		StringBuilder body = new StringBuilder("Bonjour ")
				.append(reservation.getUser() != null ? reservation.getUser().getUsername() : "client")
				.append(",\n\nNous confirmons le remboursement de votre réservation #")
				.append(reservation.getReservationId())
				.append(".");
		if (reason != null && !reason.isBlank()) {
			body.append("\nMotif: ").append(reason).append('.');
		}
		body.append("\nLes fonds seront disponibles selon les délais habituels de votre banque.");
		dispatch(reservation, subject, body.toString());
		sendRealtimePaymentNotification(reservation, NotificationSeverity.INFO, subject, body.toString(), "refunded",
				"refunded");
	}

	public void notifyFavoriteAdded(User user, Offer offer) {
		String offerLabel = offer != null ? offer.getDescription() : "une offre";
		String subject = "Nouvelle offre ajoutée à vos favoris";
		String body = "Bonjour " + (user != null ? user.getUsername() : "client")
				+ ",\n\nVous venez d'ajouter " + offerLabel
				+ " à vos favoris. Nous vous préviendrons des changements importants.";
		dispatchToUser(user, subject, body);
		sendRealtimeFavoriteNotification(user, offer, NotificationSeverity.SUCCESS, subject, body, "favorite_added");
	}

	public void notifyFavoriteRemoved(User user, Offer offer) {
		String offerLabel = offer != null ? offer.getDescription() : "une offre";
		String subject = "Offre retirée de vos favoris";
		String body = "Bonjour " + (user != null ? user.getUsername() : "client")
				+ ",\n\nVous avez retiré " + offerLabel
				+ " de vos favoris. Vous pourrez toujours la retrouver dans le catalogue.";
		dispatchToUser(user, subject, body);
		sendRealtimeFavoriteNotification(user, offer, NotificationSeverity.INFO, subject, body, "favorite_removed");
	}

	public void notifyPaymentRetryRequested(Reservation reservation) {
		String subject = "Relance de paiement - Réservation #" + reservation.getReservationId();
		String body = "Bonjour "
				+ (reservation.getUser() != null ? reservation.getUser().getUsername() : "client")
				+ ",\n\nVous avez relancé le paiement pour la réservation #" + reservation.getReservationId()
				+ ". Suivez le lien de paiement envoyé pour finaliser la transaction.\n";
		dispatch(reservation, subject, body);
		sendRealtimePaymentNotification(reservation, NotificationSeverity.INFO, subject, body, "payment_retry",
				"pending");
	}

	private void dispatch(Reservation reservation, String subject, String body) {
		dispatchToUser(reservation.getUser(), subject, body);
	}

	private void dispatchToUser(User user, String subject, String body) {
		if (mailEnabled && user != null && user.getEmail() != null) {
			try {
				SimpleMailMessage message = new SimpleMailMessage();
				message.setFrom(mailFrom);
				message.setTo(user.getEmail());
				message.setSubject(subject);
				message.setText(body);
				mailSender.send(message);
			} catch (Exception ex) {
				LOGGER.warn("Impossible d'envoyer l'email: {}", ex.getMessage());
			}
		} else {
			LOGGER.info("[MAIL MOCK] {} -> {}", subject, body);
		}

		if (smsEnabled) {
			String to = smsToFallback;
			try {
				Message.creator(new PhoneNumber(to), new PhoneNumber(smsFrom), subject).create();
			} catch (ApiException ex) {
				LOGGER.warn("Impossible d'envoyer le SMS: {}", ex.getMessage());
			}
		} else {
			LOGGER.info("[SMS MOCK] {} -> {}", subject, body);
		}
	}

	private void sendRealtimePaymentNotification(Reservation reservation, NotificationSeverity severity, String title,
			String message, String eventType, String paymentStatus) {
		if (reservation == null || reservation.getUser() == null || reservation.getUser().getId() == null) {
			return;
		}

		Map<String, String> metadata = new HashMap<>();
		metadata.put("entityType", "payment");
		metadata.put("eventType", eventType);
		metadata.put("paymentStatus", paymentStatus);
		if (reservation.getReservationId() != null) {
			metadata.put("reservationId", reservation.getReservationId().toString());
		}
		if (reservation.getOffer() != null && reservation.getOffer().getDescription() != null) {
			metadata.put("offerTitle", reservation.getOffer().getDescription());
		}

		sendRealtimeNotification(reservation.getUser(), severity, title, message, metadata);
	}

	private void sendRealtimeFavoriteNotification(User user, Offer offer, NotificationSeverity severity, String title,
			String message, String eventType) {
		if (user == null || user.getId() == null) {
			return;
		}

		Map<String, String> metadata = new HashMap<>();
		metadata.put("entityType", "offer");
		metadata.put("eventType", eventType);
		if (offer != null) {
			if (offer.getDescription() != null) {
				metadata.put("offerTitle", offer.getDescription());
			}
			if (offer.getOfferId() != null) {
				metadata.put("offerId", offer.getOfferId().toString());
			}
		}

		sendRealtimeNotification(user, severity, title, message, metadata);
	}

	private void sendRealtimeNotification(User user, NotificationSeverity severity, String title, String message,
			Map<String, String> metadata) {
		if (user == null || user.getId() == null) {
			return;
		}

		try {
			NotificationMessage notification = new NotificationMessage();
			notification.setCategory(NotificationCategory.USER_ACTION);
			notification.setSeverity(severity);
			notification.setTitle(title);
			notification.setMessage(message);
			notification.setRecipient(String.valueOf(user.getId()));

			if (metadata != null) {
				metadata.forEach((key, value) -> {
					if (value != null) {
						notification.getMetadata().put(key, value);
					}
				});
			}

			notificationStreamingService.sendNotification(notification);
		} catch (Exception ex) {
			LOGGER.warn("Impossible d'envoyer la notification temps réel: {}", ex.getMessage());
		}
	}
}
