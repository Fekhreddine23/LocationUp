package com.mobility.mobility_backend.controller.health;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityState;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ApplicationAvailability applicationAvailability;

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')") // ✅ SEULEMENT POUR ADMINS
	public Map<String, Object> healthCheck() {
		Map<String, Object> health = new HashMap<>();

		// Statut de base
		health.put("status", "UP");
		health.put("timestamp", LocalDateTime.now().toString());
		health.put("service", "LocationUp Backend");

		// Disponibilité de l'application
		AvailabilityState livenessState = applicationAvailability.getLivenessState();
		AvailabilityState readinessState = applicationAvailability.getReadinessState();
		health.put("liveness", livenessState.toString());
		health.put("readiness", readinessState.toString());

		// Check base de données
		try {
			jdbcTemplate.queryForObject("SELECT 1", Integer.class);
			health.put("database", "UP");
		} catch (Exception e) {
			health.put("database", "DOWN");
			health.put("database_error", e.getMessage());
		}

		// Métriques mémoire
		Runtime runtime = Runtime.getRuntime();
		health.put("memory", Map.of("max", runtime.maxMemory() / (1024 * 1024) + " MB", "total",
				runtime.totalMemory() / (1024 * 1024) + " MB", "free", runtime.freeMemory() / (1024 * 1024) + " MB",
				"used", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) + " MB"));

		return health;
	}

	@GetMapping("/liveness")
	@PreAuthorize("hasRole('ADMIN')")
	public Map<String, String> liveness() {
		Map<String, String> response = new HashMap<>();
		response.put("status", "UP");
		response.put("timestamp", LocalDateTime.now().toString());
		return response;
	}

	@GetMapping("/readiness")
	@PreAuthorize("hasRole('ADMIN')")
	public Map<String, String> readiness() {
		Map<String, String> response = new HashMap<>();

		try {
			jdbcTemplate.queryForObject("SELECT 1", Integer.class);
			response.put("status", "UP");
			response.put("database", "CONNECTED");
		} catch (Exception e) {
			response.put("status", "DOWN");
			response.put("database", "DISCONNECTED");
		}

		response.put("timestamp", LocalDateTime.now().toString());
		return response;
	}

}
