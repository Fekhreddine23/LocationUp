package com.mobility.mobility_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.mobility.mobility_backend.entity.Reservation;
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

	public PaymentNotificationService(JavaMailSender mailSender,
			@Value("${notifications.mail.enabled:false}") boolean mailEnabled,
			@Value("${notifications.mail.from:no-reply@locationup.test}") String mailFrom,
			@Value("${notifications.sms.enabled:false}") boolean smsEnabled,
			@Value("${twilio.account-sid:}") String twilioSid,
			@Value("${twilio.auth-token:}") String twilioToken,
			@Value("${twilio.from-number:}") String smsFrom,
			@Value("${notifications.sms.test-recipient:+33123456789}") String smsToFallback) {
		this.mailSender = mailSender;
		this.mailEnabled = mailEnabled;
		this.mailFrom = mailFrom;
		this.smsEnabled = smsEnabled && !twilioSid.isBlank() && !twilioToken.isBlank() && !smsFrom.isBlank();
		this.smsFrom = smsFrom;
		this.smsToFallback = smsToFallback;

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
	}

	public void notifyPaymentFailure(Reservation reservation, String reason) {
		String subject = "Echec de paiement - Réservation #" + reservation.getReservationId();
		String body = "Bonjour "
				+ (reservation.getUser() != null ? reservation.getUser().getUsername() : "client")
				+ ",\n\nNous n'avons pas pu traiter votre paiement. Raison: " + reason
				+ ".\nMerci de vous reconnecter pour finaliser la transaction.\n";
		dispatch(reservation, subject, body);
	}

	public void notifyUpcomingPayment(Reservation reservation) {
		String subject = "Rappel paiement - Réservation #" + reservation.getReservationId();
		String body = "Bonjour "
				+ (reservation.getUser() != null ? reservation.getUser().getUsername() : "client")
				+ ",\n\nVotre réservation est planifiée le " + reservation.getReservationDate()
				+ ". Merci de finaliser le paiement si ce n'est pas déjà fait.\n";
		dispatch(reservation, subject, body);
	}

	private void dispatch(Reservation reservation, String subject, String body) {
		if (mailEnabled && reservation.getUser() != null && reservation.getUser().getEmail() != null) {
			try {
				SimpleMailMessage message = new SimpleMailMessage();
				message.setFrom(mailFrom);
				message.setTo(reservation.getUser().getEmail());
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
}
