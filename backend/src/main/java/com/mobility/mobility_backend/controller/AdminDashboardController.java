package com.mobility.mobility_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.dto.AdminStatsDTO;
import com.mobility.mobility_backend.dto.DashboardTrendsDTO;
import com.mobility.mobility_backend.dto.RecentActivityDTO;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.service.DashboardService;
import com.mobility.mobility_backend.service.cache.AdminStatsCacheService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@Tag(name = "Admin Dashboard", description = "Endpoints pour le tableau de bord administrateur")
public class AdminDashboardController {

	private final DashboardService dashboardService;

	@Autowired
	private AdminStatsCacheService adminStatsCacheService;

	@Autowired
	public AdminDashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping("/stats")
	@Operation(summary = "Récupérer les statistiques globales", description = "Retourne les KPIs principaux (CA, utilisateurs, etc.)")
	public ResponseEntity<AdminStatsDTO> getDashboardStats() {
		log.debug("Récupération des statistiques admin");
		AdminStatsDTO stats = dashboardService.getAdminStats();
		return ResponseEntity.ok(stats);
	}

	@GetMapping("/stats/trends")
	@Operation(summary = "Récupérer les tendances", description = "Retourne l'évolution des chiffres sur les X derniers mois")
	public ResponseEntity<DashboardTrendsDTO> getDashboardTrends(@RequestParam(value = "months", defaultValue = "6") int months) {
		return ResponseEntity.ok(dashboardService.getDashboardTrends(months));
	}


	/**
	 * Récupère les utilisateurs paginés
	 */
	@GetMapping("/dashboard/stats")
	public ResponseEntity<Page<User>> getUsers(Pageable pageable) {
		log.debug("Consultation liste utilisateurs page {} size {}", pageable.getPageNumber(), pageable.getPageSize());
		Page<User> users = dashboardService.getUsers(pageable);
		return ResponseEntity.ok(users);
	}

	/**
	 * Récupère l'activité récente AVEC CACHE
	 */
	@GetMapping("/recent-activity")
	public ResponseEntity<List<RecentActivityDTO>> getRecentActivity() {
		List<RecentActivityDTO> activity = dashboardService.getRecentActivity();
		return ResponseEntity.ok(activity);
	}

	/**
	 * Endpoint pour invalider manuellement le cache
	 */
	@PostMapping("/cache/invalidate")
	public ResponseEntity<String> invalidateCache() {
		adminStatsCacheService.invalidateAllAdminCache();
		return ResponseEntity.ok("✅ Cache admin invalidé avec succès");
	}

	/**
	 * Endpoint pour vérifier l'état du cache
	 */
	@GetMapping("/cache/status")
	public ResponseEntity<String> getCacheStatus() {
		// Vérifier si des données sont en cache
		AdminStatsDTO cachedStats = adminStatsCacheService.getCachedStats();
		boolean hasCachedStats = cachedStats != null;

		String status = String.format("""
			📊 État du Cache Admin:
			✅ Statistiques en cache: %s
			🕒 Dernière mise à jour: %s
			""",
			hasCachedStats ? "OUI" : "NON",
			hasCachedStats ? cachedStats.getLastUpdated() : "N/A"
		);

		return ResponseEntity.ok(status);
	}

}
