package com.mobility.mobility_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.dto.identity.IdentitySessionRequest;
import com.mobility.mobility_backend.dto.identity.IdentitySessionResponse;
import com.mobility.mobility_backend.dto.identity.IdentityStatusResponse;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.UserRepository;
import com.mobility.mobility_backend.service.IdentityVerificationService;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/api/identity")
@PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
public class IdentityController {

	private final IdentityVerificationService identityVerificationService;
	private final UserRepository userRepository;

	public IdentityController(IdentityVerificationService identityVerificationService,
			UserRepository userRepository) {
		this.identityVerificationService = identityVerificationService;
		this.userRepository = userRepository;
	}

	@PostMapping("/session")
	public ResponseEntity<IdentitySessionResponse> createSession(
			@RequestBody(required = false) IdentitySessionRequest request) throws StripeException {
		Integer userId = resolveCurrentUserId();
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		String returnUrl = request != null ? request.getReturnUrl() : null;
		IdentitySessionResponse response = identityVerificationService.startVerification(userId,
				request != null ? request.getReservationId() : null,
				request != null ? request.getDocumentType() : null,
				returnUrl);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/status")
	public ResponseEntity<IdentityStatusResponse> getStatus() {
		Integer userId = resolveCurrentUserId();
		if (userId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok(identityVerificationService.getStatus(userId));
	}

	private Integer resolveCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null) {
			return null;
		}
		return userRepository.findByUsername(authentication.getName()).map(User::getId).orElse(null);
	}
}
