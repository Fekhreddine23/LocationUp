package com.mobility.mobility_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.service.StripeIdentityWebhookService;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/api/identity/webhook")
public class IdentityWebhookController {

	private final StripeIdentityWebhookService webhookService;

	public IdentityWebhookController(StripeIdentityWebhookService webhookService) {
		this.webhookService = webhookService;
	}

	@PostMapping
	public ResponseEntity<String> handleWebhook(@RequestBody String payload,
			@RequestHeader(value = "Stripe-Signature", required = false) String signature) throws StripeException {
		webhookService.handleEvent(payload, signature);
		return ResponseEntity.ok("received");
	}
}
