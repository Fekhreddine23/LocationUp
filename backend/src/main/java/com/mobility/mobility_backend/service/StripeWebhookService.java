package com.mobility.mobility_backend.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mobility.mobility_backend.entity.PaymentEventLog;
import com.mobility.mobility_backend.entity.Reservation;
import com.mobility.mobility_backend.repository.PaymentEventLogRepository;
import com.mobility.mobility_backend.repository.ReservationRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

@Service
public class StripeWebhookService {

	private final ReservationRepository reservationRepository;
	private final PaymentEventLogRepository logRepository;
	private final PaymentNotificationService notificationService;

	@Value("${stripe.webhook.secret:}")
	private String webhookSecret;

	public StripeWebhookService(ReservationRepository reservationRepository, PaymentEventLogRepository logRepository,
			PaymentNotificationService notificationService) {
		this.reservationRepository = reservationRepository;
		this.logRepository = logRepository;
		this.notificationService = notificationService;
	}

	public void handleWebhook(String payload, String signature) throws StripeException {
		PaymentEventLog log = new PaymentEventLog();
		log.setPayload(payload);
		log.setReceivedAt(LocalDateTime.now());

		try {
			Event event = parseEvent(payload, signature);
			log.setEventId(event.getId());
			log.setType(event.getType());

			switch (event.getType()) {
			case "checkout.session.completed":
				handleCheckoutCompleted(event, log);
				break;
			case "payment_intent.payment_failed":
				handlePaymentFailed(event, log);
				break;
			default:
				log.setStatus("IGNORED");
			}

			if (log.getStatus() == null) {
				log.setStatus("PROCESSED");
			}
		} catch (StripeException ex) {
			log.setStatus("FAILED");
			log.setErrorMessage(ex.getMessage());
			throw ex;
		} catch (RuntimeException ex) {
			log.setStatus("FAILED");
			log.setErrorMessage(ex.getMessage());
			throw ex;
		} finally {
			logRepository.save(log);
		}
	}

	private Event parseEvent(String payload, String signature) throws SignatureVerificationException {
		if (webhookSecret == null || webhookSecret.isBlank()) {
			return Event.GSON.fromJson(payload, Event.class);
		}
		return Webhook.constructEvent(payload, signature, webhookSecret);
	}

	private void handleCheckoutCompleted(Event event, PaymentEventLog log) {
		Optional<Session> sessionOpt = deserializeSession(event);
		if (sessionOpt.isEmpty()) {
			log.setStatus("FAILED");
			log.setErrorMessage("Session introuvable dans l'événement");
			return;
		}

		Session session = sessionOpt.get();
		String referenceId = session.getClientReferenceId();
		log.setReservationReference(referenceId);

		Optional<Reservation> optional = parseReservation(referenceId);
		if (optional.isEmpty()) {
			log.setStatus("PENDING_RESERVATION");
			log.setErrorMessage("Réservation introuvable pour reference " + referenceId);
			return;
		}

		Reservation reservation = optional.get();
		LocalDateTime now = LocalDateTime.now();
		reservation.setPaymentStatus(Reservation.PaymentStatus.PAID);
		reservation.setPaymentReference(session.getId());
		reservation.setPaymentDate(now);
		reservation.setUpdatedAt(now);
		if (reservation.getPaymentAmount() == null && reservation.getOffer() != null) {
			reservation.setPaymentAmount(reservation.getOffer().getPrice());
		}
		reservationRepository.save(reservation);
		notificationService.notifyPaymentSuccess(reservation);
		log.setStatus("PROCESSED");
	}

	private void handlePaymentFailed(Event event, PaymentEventLog log) {
		PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer().getObject()
				.orElse(null);

		String reference = intent != null ? intent.getClientSecret() : null;
		log.setReservationReference(reference);

		Optional<Reservation> optional = parseReservation(reference);
		if (optional.isEmpty()) {
			log.setStatus("PENDING_RESERVATION");
			log.setErrorMessage("Réservation introuvable pour reference " + reference);
			return;
		}

		Reservation reservation = optional.get();
		reservation.setPaymentStatus(Reservation.PaymentStatus.FAILED);
		reservation.setUpdatedAt(LocalDateTime.now());
		reservationRepository.save(reservation);
		String reason = intent != null && intent.getLastPaymentError() != null
				? intent.getLastPaymentError().getMessage()
				: "Erreur inconnue";
		notificationService.notifyPaymentFailure(reservation, reason);
		log.setStatus("PROCESSED");
		log.setErrorMessage(reason);
	}

	private Optional<Reservation> parseReservation(String reference) {
		if (reference == null) {
			return Optional.empty();
		}
		try {
			Integer reservationId = Integer.parseInt(reference);
			return reservationRepository.findById(reservationId);
		} catch (NumberFormatException ex) {
			return reservationRepository.findByPaymentReference(reference);
		}
	}

	private Optional<Session> deserializeSession(Event event) {
		return event.getDataObjectDeserializer().getObject()
				.filter(Session.class::isInstance)
				.map(Session.class::cast)
				.or(() -> Optional.ofNullable(event.getDataObjectDeserializer().getRawJson())
						.map(json -> (Session) Event.GSON.fromJson(json, Session.class)))
				.or(() -> Optional.ofNullable(event.getData().getObject())
						.map(this::extractSessionId)
						.map(id -> {
							try {
								return Session.retrieve(id);
							} catch (StripeException e) {
								throw new IllegalStateException("Impossible de récupérer la session " + id, e);
							}
						}));
	}

	private String extractSessionId(Object dataObject) {
		if (dataObject == null) {
			return null;
		}
		if (dataObject instanceof java.util.Map<?, ?> map) {
			Object id = map.get("id");
			return id != null ? id.toString() : null;
		}
		if (dataObject instanceof com.stripe.model.HasId hasId) {
			return hasId.getId();
		}
		return null;
	}
}
