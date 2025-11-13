package com.mobility.mobility_backend.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.entity.Admin;
import com.mobility.mobility_backend.repository.AdminRepository;

@RestController
@RequestMapping("/api/debug")
public class debugController { // ⚠️ Correction : "DebugController" avec majuscule

	private final PasswordEncoder passwordEncoder;
	private final AdminRepository adminRepository;

	// ⚠️ UN SEUL CONSTRUCTEUR correct
	public debugController(PasswordEncoder passwordEncoder, AdminRepository adminRepository) {
		this.passwordEncoder = passwordEncoder;
		this.adminRepository = adminRepository;
		System.out.println("🎉 [DebugController] INITIALIZED!");
	}

	@PostMapping("/test-post")
	public ResponseEntity<Map<String, Object>> testPost() {
		System.out.println("✅ [DebugController] POST endpoint called!");
		return ResponseEntity
				.ok(Map.of("message", "DebugController POST works!", "timestamp", LocalDateTime.now().toString()));
	}

	@GetMapping("/test-get")
	public ResponseEntity<Map<String, Object>> testGet() {
		System.out.println("✅ [DebugController] GET endpoint called!");
		return ResponseEntity
				.ok(Map.of("message", "DebugController GET works!", "timestamp", LocalDateTime.now().toString()));
	}

	@GetMapping("/test-password")
	public Map<String, Object> testPassword() {
		System.out.println("🔍 [DebugController] Testing password for admin...");

		Optional<Admin> admin = adminRepository.findByUsername("admin");
		Map<String, Object> result = new HashMap<>();

		if (admin.isPresent()) {
			String storedHash = admin.get().getPassword();
			boolean matches = passwordEncoder.matches("password", storedHash);

			result.put("username", "admin");
			result.put("storedHash", storedHash);
			result.put("testPassword", "password");
			result.put("matches", matches);
			result.put("encoderClass", passwordEncoder.getClass().getName());

			System.out.println("✅ [DebugController] Password test result: " + matches);
		} else {
			result.put("error", "Admin not found");
			System.out.println("❌ [DebugController] Admin not found");
		}

		return result;
	}
}