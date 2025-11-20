package com.mobility.mobility_backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobility.mobility_backend.dto.AdminStatsDTO;
import com.mobility.mobility_backend.dto.CategoryStatDTO;
import com.mobility.mobility_backend.dto.CityStatDTO;
import com.mobility.mobility_backend.dto.DashboardTrendsDTO;
import com.mobility.mobility_backend.dto.MonthlyReservationStatDTO;
import com.mobility.mobility_backend.dto.RecentActivityDTO;
import com.mobility.mobility_backend.dto.UserInfoDTO;
import com.mobility.mobility_backend.entity.Reservation;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.OfferRepository;
import com.mobility.mobility_backend.repository.ReservationRepository;
import com.mobility.mobility_backend.repository.UserRepository;
import com.mobility.mobility_backend.service.cache.AdminStatsCacheService;

@Service
@Transactional
public class DashboardService {

	private final UserRepository userRepository;
	private final ReservationRepository reservationRepository;
	private final OfferRepository offerRepository;
	

	@Autowired
	private AdminStatsCacheService adminStatsCacheService;

	@Autowired
	public DashboardService(UserRepository userRepository, ReservationRepository reservationRepository,
			OfferRepository offerRepository) {
		this.userRepository = userRepository;
		this.reservationRepository = reservationRepository;
		this.offerRepository = offerRepository;
	}

	/**
	 * Récupère les statistiques admin AVEC CACHE
	 */
	public AdminStatsDTO getAdminStats() {
		// Vérifier d'abord le cache
		AdminStatsDTO cachedStats = adminStatsCacheService.getCachedStats();
		if (cachedStats != null) {
			System.out.println("✅ Statistiques récupérées du CACHE");
			return cachedStats;
		}

		// Si pas en cache, calculer les statistiques
		AdminStatsDTO stats = calculateAdminStats();
		
		// Mettre en cache
		adminStatsCacheService.cacheStats(stats);
		System.out.println("💾 Statistiques mises en CACHE");
		
		return stats;
	}

	/**
	 * Calcule les statistiques admin (version optimisée)
	 */
	private AdminStatsDTO calculateAdminStats() {
		long totalUsers = userRepository.count();
		long activeUsers = totalUsers;
		long totalReservations = reservationRepository.count();
		long pendingReservations = reservationRepository.countByStatus(Reservation.ReservationStatus.PENDING);
		long totalOffers = offerRepository.count();
		double totalRevenue = calculateMockRevenue();

		// Utilisez le constructeur avec lastUpdated
		return new AdminStatsDTO(
			totalUsers, 
			activeUsers, 
			totalReservations, 
			pendingReservations, 
			totalOffers, 
			totalRevenue, 
			LocalDateTime.now() // ← Date de mise à jour
		);
	}

	public Page<User> getUsers(Pageable pageable) {
		return userRepository.findAll(pageable);
	}

	/**
	 * Récupère l'activité récente AVEC CACHE
	 */
	public List<RecentActivityDTO> getRecentActivity() {
		// Vérifier d'abord le cache
		Object cachedActivity = adminStatsCacheService.getCachedRecentActivity();
		if (cachedActivity instanceof List) {
			System.out.println("✅ Activité récente récupérée du CACHE");
			return (List<RecentActivityDTO>) cachedActivity;
		}

		// Si pas en cache, récupérer depuis la base
		List<RecentActivityDTO> activity = fetchRecentActivity();
		
		// Mettre en cache
		adminStatsCacheService.cacheRecentActivity(activity);
		System.out.println("💾 Activité récente mise en CACHE");
		
		return activity;
	}

	/**
	 * Récupère l'activité récente depuis la base de données
	 */
	private List<RecentActivityDTO> fetchRecentActivity() {
		// Récupérer les réservations récentes
		List<Reservation> recentReservations = reservationRepository.findTop10ByOrderByReservationDateDesc();

		return recentReservations.stream()
				.map(reservation -> new RecentActivityDTO(reservation.getReservationId().longValue(), "RESERVATION",
						"Nouvelle réservation créée par " + reservation.getUser().getUsername(),
						reservation.getReservationDate(), new UserInfoDTO(
								reservation.getUser().getId().longValue(), reservation.getUser().getUsername(),
								reservation.getUser().getEmail())))
				.collect(Collectors.toList());
	}

	/**
	 * Invalide le cache admin (à appeler quand les données changent)
	 */
	public void invalidateAdminCache() {
		adminStatsCacheService.invalidateAllAdminCache();
		System.out.println("🗑️ Cache dashboard invalidé");
	}

	/**
	 * Invalide seulement le cache des statistiques
	 */
	public void invalidateStatsCache() {
		adminStatsCacheService.invalidateStatsCache();
		System.out.println("🗑️ Cache statistiques invalidé");
	}

	private double calculateMockRevenue() {
		// Pour l'instant, retourne une valeur mockée
		// Plus tard, tu pourras implémenter la vraie logique
		return 8450.0;
	}

	/**
	 * Méthode pour forcer le recalcul des stats (invalide et recalcule)
	 */
	public AdminStatsDTO refreshAdminStats() {
		// Invalider le cache
		invalidateStatsCache();
		
		// Recalculer et remettre en cache
		return getAdminStats();
	}

	public DashboardTrendsDTO getDashboardTrends(int months) {
		int range = Math.max(1, Math.min(months, 12));
		List<MonthlyReservationStatDTO> monthlyStats = buildMonthlyReservationStats(range);
		List<CategoryStatDTO> categoryStats = buildCategoryStats();
		List<CityStatDTO> cityStats = buildCityStats();
		return new DashboardTrendsDTO(monthlyStats, categoryStats, cityStats);
	}

	private List<MonthlyReservationStatDTO> buildMonthlyReservationStats(int months) {
		Map<YearMonth, MonthlyReservationStatDTO> statsMap = new LinkedHashMap<>();
		YearMonth current = YearMonth.now();
		for (int i = months - 1; i >= 0; i--) {
			YearMonth month = current.minusMonths(i);
			String label = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH) + " " + month.getYear();
			statsMap.put(month, new MonthlyReservationStatDTO(label, 0, 0));
		}

		reservationRepository.findAll().forEach(reservation -> {
			YearMonth month = YearMonth.from(reservation.getReservationDate());
			if (statsMap.containsKey(month)) {
				MonthlyReservationStatDTO stat = statsMap.get(month);
				stat.setReservations(stat.getReservations() + 1);
				BigDecimal price = reservation.getOffer() != null && reservation.getOffer().getPrice() != null
						? reservation.getOffer().getPrice()
						: BigDecimal.ZERO;
				stat.setRevenue(round(stat.getRevenue() + price.doubleValue()));
			}
		});

		return new ArrayList<>(statsMap.values());
	}

	private List<CategoryStatDTO> buildCategoryStats() {
		Map<String, Long> counts = offerRepository.findAll().stream()
				.collect(Collectors.groupingBy(
						offer -> {
							if (offer.getMobilityService() != null
									&& offer.getMobilityService().getCategorie() != null
									&& !offer.getMobilityService().getCategorie().isBlank()) {
								return offer.getMobilityService().getCategorie();
							}
							return "Autre";
						},
						Collectors.counting()));

		return counts.entrySet().stream()
				.map(entry -> new CategoryStatDTO(entry.getKey(), entry.getValue()))
				.sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
				.collect(Collectors.toList());
	}

	private List<CityStatDTO> buildCityStats() {
		Map<String, Long> counts = offerRepository.findAll().stream()
				.collect(Collectors.groupingBy(
						offer -> offer.getPickupLocation() != null ? offer.getPickupLocation().getName() : "Inconnu",
						Collectors.counting()));

		return counts.entrySet().stream()
				.sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
				.limit(5)
				.map(entry -> new CityStatDTO(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());
	}

	private double round(double value) {
		return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
}
