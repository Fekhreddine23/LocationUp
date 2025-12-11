package com.mobility.mobility_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.identity.VerificationSession;
import com.stripe.net.Webhook;

@Service
public class StripeIdentityWebhookService {

	private final IdentityVerificationService identityVerificationService;

	@Value("${stripe.identity.webhook-secret:}")
	private String identityWebhookSecret;

	public StripeIdentityWebhookService(IdentityVerificationService identityVerificationService) {
		this.identityVerificationService = identityVerificationService;
	}

	public void handleEvent(String payload, String signature) throws StripeException {
		Event event = parseEvent(payload, signature);
		if (event == null || event.getType() == null
				|| !event.getType().startsWith("identity.verification_session")) {
			return;
		}

		VerificationSession session = extractSession(event);
		identityVerificationService.updateVerificationFromSession(session);
	}

	private Event parseEvent(String payload, String signature) throws SignatureVerificationException {
		if (identityWebhookSecret == null || identityWebhookSecret.isBlank()) {
			return Event.GSON.fromJson(payload, Event.class);
		}
		return Webhook.constructEvent(payload, signature, identityWebhookSecret);
	}

	private VerificationSession extractSession(Event event) throws StripeException {
		return event.getDataObjectDeserializer().getObject()
				.filter(VerificationSession.class::isInstance)
				.map(VerificationSession.class::cast)
				.orElseGet(() -> Event.GSON.fromJson(event.getDataObjectDeserializer().getRawJson(),
						VerificationSession.class));
	}
}
