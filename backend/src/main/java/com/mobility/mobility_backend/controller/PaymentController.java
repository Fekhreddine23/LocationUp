package com.mobility.mobility_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.dto.identity.IdentitySessionRequest;
import com.mobility.mobility_backend.dto.identity.IdentitySessionResponse;
import com.mobility.mobility_backend.dto.payment.PaymentSessionRequest;
import com.mobility.mobility_backend.dto.payment.PaymentSessionResponse;
import com.mobility.mobility_backend.dto.payment.PaymentStatusResponse;
import com.mobility.mobility_backend.entity.Reservation;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.ReservationRepository;
import com.mobility.mobility_backend.repository.UserRepository;
import com.mobility.mobility_backend.service.IdentityVerificationService;
import com.mobility.mobility_backend.service.PaymentService;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/api/payments")
@PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
public class PaymentController {

	private final PaymentService paymentService;
	private final IdentityVerificationService identityVerificationService;
	private final ReservationRepository reservationRepository;
	private final UserRepository userRepository;

	public PaymentController(PaymentService paymentService,
			IdentityVerificationService identityVerificationService,
			ReservationRepository reservationRepository,
			UserRepository userRepository) {
		this.paymentService = paymentService;
		this.identityVerificationService = identityVerificationService;
		this.reservationRepository = reservationRepository;
		this.userRepository = userRepository;
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

	@PostMapping("/identity-session")
	public ResponseEntity<IdentitySessionResponse> createIdentitySessionViaPayment(
			@RequestBody(required = false) IdentitySessionRequest request) throws StripeException {
		Integer userId = resolveTargetUser(request);
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		IdentitySessionResponse response = identityVerificationService.startVerification(userId,
				request != null ? request.getReservationId() : null,
				request != null ? request.getDocumentType() : null,
				request != null ? request.getReturnUrl() : null);
		return ResponseEntity.ok(response);
	}

	private Integer resolveTargetUser(IdentitySessionRequest request) {
		if (request != null && request.getUserId() != null) {
			return request.getUserId();
		}
		if (request != null && request.getReservationId() != null) {
			return reservationRepository.findById(request.getReservationId())
					.map(Reservation::getUser)
					.map(User::getId)
					.orElse(null);
		}
		return resolveCurrentUserId();
	}

	private Integer resolveCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null) {
			return null;
		}
		return userRepository.findByUsername(authentication.getName()).map(User::getId).orElse(null);
	}
}
