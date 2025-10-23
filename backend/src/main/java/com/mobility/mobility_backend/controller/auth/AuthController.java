package com.mobility.mobility_backend.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.dto.auth.AuthenticationRequest;
import com.mobility.mobility_backend.dto.auth.AuthenticationResponse;
import com.mobility.mobility_backend.dto.auth.RegisterRequest;
import com.mobility.mobility_backend.entity.Role;
import com.mobility.mobility_backend.service.auth.AuthenticationService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationService authenticationService;

	public AuthController(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
		// Par défaut, on crée un USER si aucun rôle n'est spécifié
		if (request.getRole() == null) {
			request.setRole(Role.ROLE_USER);
		}
		return ResponseEntity.ok(authenticationService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
		return ResponseEntity.ok(authenticationService.authenticate(request));
	}
}
