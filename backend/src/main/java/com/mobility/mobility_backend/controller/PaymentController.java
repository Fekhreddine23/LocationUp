package com.mobility.mobility_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.dto.payment.PaymentSessionRequest;
import com.mobility.mobility_backend.dto.payment.PaymentSessionResponse;
import com.mobility.mobility_backend.dto.payment.PaymentStatusResponse;
import com.mobility.mobility_backend.service.PaymentService;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/api/payments")
@PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
public class PaymentController {

	private final PaymentService paymentService;

	public PaymentController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	@PostMapping("/session")
	public ResponseEntity<PaymentSessionResponse> createSession(@RequestBody PaymentSessionRequest request)
			throws StripeException {
		return ResponseEntity.ok(paymentService.createCheckoutSession(request));
	}

	@PostMapping("/session/{reservationId}/sync")
	public ResponseEntity<PaymentStatusResponse> syncPayment(@PathVariable Integer reservationId)
			throws StripeException {
		return ResponseEntity.ok(paymentService.syncPaymentStatus(reservationId));
	}
}
