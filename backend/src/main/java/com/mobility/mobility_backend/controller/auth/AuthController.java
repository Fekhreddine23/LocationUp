package com.mobility.mobility_backend.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mobility.mobility_backend.dto.auth.AuthenticationRequest;
import com.mobility.mobility_backend.dto.auth.AuthenticationResponse;
import com.mobility.mobility_backend.dto.auth.RegisterRequest;
import com.mobility.mobility_backend.entity.Role;
import com.mobility.mobility_backend.service.auth.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

	private final AuthenticationService authenticationService;
	private final HttpServletRequest httpServletRequest;

	public AuthController(AuthenticationService authenticationService, HttpServletRequest httpServletRequest) {
		this.authenticationService = authenticationService;
		this.httpServletRequest = httpServletRequest;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
		String userAgent = httpServletRequest.getHeader("User-Agent");
		String ip = httpServletRequest.getHeader("X-Forwarded-For");
		if (ip == null || ip.isBlank()) {
			ip = httpServletRequest.getRemoteAddr();
		}

		try {
			if (request == null) {
				logger.warn("Register attempt with empty body: ip={}, ua={}", ip, userAgent);
				throw new RuntimeException("Requête d'inscription vide");
			}
			// Par défaut, on crée un USER si aucun rôle n'est spécifié
			if (request.getRole() == null) {
				request.setRole(Role.ROLE_USER);
			}
			logger.info("Register attempt: username={}, email={}, role={}, ip={}, ua={}",
					maskUsername(request.getUsername()),
					maskEmail(request.getEmail()),
					request.getRole(),
					ip,
					userAgent);

			AuthenticationResponse response = authenticationService.register(request);
			ResponseCookie refreshCookie = authenticationService.buildRefreshCookie(response.getRefreshToken());
			return ResponseEntity.ok()
					.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
					.body(response);
		} catch (Exception ex) {
			logger.warn("Register failed: username={}, email={}, role={}, ip={}, ua={}, error={}",
					maskUsername(request != null ? request.getUsername() : null),
					maskEmail(request != null ? request.getEmail() : null),
					request != null ? request.getRole() : null,
					ip,
					userAgent,
					ex.getMessage());
			throw ex;
		}

	}

	@PostMapping("/login")
	public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
		AuthenticationResponse response = authenticationService.authenticate(request);
		ResponseCookie refreshCookie = authenticationService.buildRefreshCookie(response.getRefreshToken());
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				.body(response);
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthenticationResponse> refreshToken(@RequestBody(required = false) AuthenticationRequest ignored) {
		AuthenticationResponse response = authenticationService.refreshAccessToken();
		ResponseCookie refreshCookie = authenticationService.buildRefreshCookie(response.getRefreshToken());
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
				.body(response);
	}

	private static String maskUsername(String username) {
		if (username == null || username.isBlank()) {
			return "null";
		}
		String trimmed = username.trim();
		if (trimmed.length() <= 2) {
			return trimmed.charAt(0) + "***";
		}
		return trimmed.charAt(0) + "***(" + trimmed.length() + ")";
	}

	private static String maskEmail(String email) {
		if (email == null || email.isBlank()) {
			return "null";
		}
		String trimmed = email.trim();
		int atIndex = trimmed.indexOf('@');
		if (atIndex <= 1 || atIndex == trimmed.length() - 1) {
			return trimmed.charAt(0) + "***";
		}
		String local = trimmed.substring(0, atIndex);
		String domain = trimmed.substring(atIndex + 1);
		return local.charAt(0) + "***@" + domain;
	}
}
