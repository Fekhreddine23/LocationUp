package com.mobility.mobility_backend.controller.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.service.auth.TwoFactorAuthService;

@RestController
@RequestMapping("/api/2fa")
public class TwoFactorAuthController {

	@Autowired
	private TwoFactorAuthService twoFactorAuthService;

	/**
	 * Génère un secret 2FA et QR Code
	 */
	@PostMapping("/setup")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> setup2FA(@RequestParam String username) {
		try {
			Map<String, String> result = twoFactorAuthService.generate2FASecret(username);
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Erreur configuration 2FA: " + e.getMessage());
		}
	}

	/**
	 * Vérifie un code 2FA
	 */
	@PostMapping("/verify")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> verify2FA(@RequestParam String username, @RequestParam String code) {

		boolean isValid = twoFactorAuthService.verifyCode(username, code);

		Map<String, Object> response = new HashMap<>();
		response.put("valid", isValid);

		if (isValid) {
			twoFactorAuthService.enable2FA(username);
			response.put("message", "2FA activé avec succès");
		} else {
			response.put("message", "Code invalide");
		}

		return ResponseEntity.ok(response);
	}

	/**
	 * Vérifie le statut 2FA
	 */
	@GetMapping("/status")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> get2FAStatus(@RequestParam String username) {
		boolean isEnabled = twoFactorAuthService.is2FAEnabled(username);

		Map<String, Object> response = new HashMap<>();
		response.put("enabled", isEnabled);
		response.put("username", username);

		return ResponseEntity.ok(response);
	}

	/**
	 * Désactive le 2FA
	 */
	@PostMapping("/disable")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> disable2FA(@RequestParam String username) {
		twoFactorAuthService.disable2FA(username);

		Map<String, String> response = new HashMap<>();
		response.put("message", "2FA désactivé pour " + username);

		return ResponseEntity.ok(response);
	}

	/**
	 * Génère un code de secours
	 */
	@GetMapping("/backup-code")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> generateBackupCode() {
		String backupCode = twoFactorAuthService.generateBackupCode();

		Map<String, String> response = new HashMap<>();
		response.put("backupCode", backupCode);
		response.put("message", "Conservez ce code en lieu sûr");

		return ResponseEntity.ok(response);
	}

}
