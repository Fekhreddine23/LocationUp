package com.mobility.mobility_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.dto.identity.IdentityDocumentDTO;
import com.mobility.mobility_backend.dto.identity.IdentitySessionRequest;
import com.mobility.mobility_backend.dto.identity.IdentitySessionResponse;
import com.mobility.mobility_backend.dto.identity.IdentityVerificationRecordDTO;
import com.mobility.mobility_backend.service.IdentityVerificationService;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/api/admin/identity")
@PreAuthorize("hasRole('ADMIN')")
public class AdminIdentityController {

	private final IdentityVerificationService identityVerificationService;

	public AdminIdentityController(IdentityVerificationService identityVerificationService) {
		this.identityVerificationService = identityVerificationService;
	}

	@GetMapping("/users/{userId}/documents")
	public ResponseEntity<List<IdentityDocumentDTO>> getDocuments(@PathVariable Integer userId) {
		return ResponseEntity.ok(identityVerificationService.getDocumentsForUser(userId));
	}

	@GetMapping("/users/{userId}/verifications")
	public ResponseEntity<List<IdentityVerificationRecordDTO>> getVerificationHistory(@PathVariable Integer userId) {
		return ResponseEntity.ok(identityVerificationService.getVerificationHistory(userId));
	}

	@PostMapping("/users/{userId}/sessions")
	public ResponseEntity<IdentitySessionResponse> startSession(@PathVariable Integer userId,
			@RequestBody(required = false) IdentitySessionRequest request) throws StripeException {
		IdentitySessionResponse response = identityVerificationService.startVerification(userId,
				request != null ? request.getReservationId() : null,
				request != null ? request.getDocumentType() : null,
				request != null ? request.getReturnUrl() : null);
		return ResponseEntity.ok(response);
	}
}
