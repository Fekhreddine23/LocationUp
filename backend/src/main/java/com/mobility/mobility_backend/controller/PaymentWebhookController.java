package com.mobility.mobility_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.service.StripeWebhookService;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/api/payments")
public class PaymentWebhookController {

	private final StripeWebhookService stripeWebhookService;

	public PaymentWebhookController(StripeWebhookService stripeWebhookService) {
		this.stripeWebhookService = stripeWebhookService;
	}

	@PostMapping("/webhook")
	public ResponseEntity<String> handleWebhook(@RequestBody String payload,
			@RequestHeader(name = "Stripe-Signature", required = false) String signature) throws StripeException {
		stripeWebhookService.handleWebhook(payload, signature);
		return ResponseEntity.ok("received");
	}
}
