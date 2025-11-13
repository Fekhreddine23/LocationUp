package com.mobility.mobility_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobility.mobility_backend.dto.AdminStatsDTO;
import com.mobility.mobility_backend.dto.UserDTO;
import com.mobility.mobility_backend.entity.Reservation;
import com.mobility.mobility_backend.entity.Role;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.UserRepository;
import com.mobility.mobility_backend.repository.ReservationRepository;
import com.mobility.mobility_backend.service.cache.AdminStatsCacheService;

@Service
@Transactional
public class AdminUserService {

	private final UserRepository userRepository;
	
	@Autowired
	private ReservationRepository reservationRepository;
	
	@Autowired
	private AdminStatsCacheService adminStatsCacheService;

	@Autowired
	public AdminUserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * Récupère tous les utilisateurs avec pagination ET cache
	 */
	public List<UserDTO> getAllUsers(int page, int size) {
		// Vérifier d'abord le cache
		Object cachedUsers = adminStatsCacheService.getCachedUsers(page, size);
		if (cachedUsers instanceof List) {
			System.out.println("✅ Utilisateurs récupérés du CACHE - Page: " + page + ", Size: " + size);
			return (List<UserDTO>) cachedUsers;
		}

		// Si pas en cache, récupérer depuis la base
		Pageable pageable = PageRequest.of(page, size);
		Page<User> userPage = userRepository.findAll(pageable);
		List<UserDTO> users = userPage.getContent().stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
		
		// Mettre en cache
		adminStatsCacheService.cacheUsers(page, size, users);
		System.out.println("💾 Utilisateurs mis en CACHE - Page: " + page + ", Size: " + size);
		
		return users;
	}

	/**
	 * Récupère les statistiques admin avec cache
	 */
	public AdminStatsDTO getAdminStats() {
		// Vérifier d'abord le cache
		AdminStatsDTO cachedStats = adminStatsCacheService.getCachedStats();
		if (cachedStats != null) {
			System.out.println("✅ Statistiques récupérées du CACHE");
			return cachedStats;
		}

		// Si pas en cache, calculer
		AdminStatsDTO stats = calculateAdminStats();
		
		// Mettre en cache
		adminStatsCacheService.cacheStats(stats);
		System.out.println("💾 Statistiques mises en CACHE");
		
		return stats;
	}

	/**
	 * Calcule les statistiques admin
	 */
	private AdminStatsDTO calculateAdminStats() {
		Long totalUsers = userRepository.count();
		Long totalReservations = reservationRepository.count();
		Long activeReservations = reservationRepository.countByStatus(Reservation.ReservationStatus.CONFIRMED);
		//Double totalRevenue = reservationRepository.getTotalRevenue();

		return new AdminStatsDTO(
		);
	}

	public Optional<UserDTO> getUserById(Integer id) {
		return userRepository.findById(id).map(this::convertToDTO);
	}

	/**
	 * Change le rôle d'un utilisateur ET invalide le cache
	 */
	public UserDTO changeUserRole(Integer userId, String newRole) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));

		try {
			Role role = Role.valueOf(newRole.toUpperCase());
			user.setRole(role);
			User updatedUser = userRepository.save(user);
			
			// Invalider le cache car les données ont changé
			invalidateUserCache();
			System.out.println("🗑️ Cache invalidé après changement de rôle");
			
			return convertToDTO(updatedUser);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Rôle invalide: " + newRole + ". Rôles valides: ROLE_USER, ROLE_ADMIN");
		}
	}

	/**
	 * Met à jour un utilisateur ET invalide le cache
	 */
	public UserDTO updateUser(Integer userId, UserDTO userDTO) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));

		// Mettre à jour les champs modifiables
		if (userDTO.getUsername() != null) {
			user.setUsername(userDTO.getUsername());
		}
		if (userDTO.getFirstname() != null) {
			user.setFirstName(userDTO.getFirstname());
		}
		if (userDTO.getLastname() != null) {
			user.setLastName(userDTO.getLastname());
		}
		if (userDTO.getEmail() != null) {
			user.setEmail(userDTO.getEmail());
		}
		if (userDTO.getRole() != null) {
			try {
				Role role = Role.valueOf(userDTO.getRole().toUpperCase());
				user.setRole(role);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Rôle invalide: " + userDTO.getRole());
			}
		}

		User updatedUser = userRepository.save(user);
		
		// Invalider le cache car les données ont changé
		invalidateUserCache();
		System.out.println("🗑️ Cache invalidé après mise à jour utilisateur");
		
		return convertToDTO(updatedUser);
	}

	/**
	 * Désactive un utilisateur ET invalide le cache
	 */
	public void deactivateUser(Integer userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));
		// Implémentez la logique de désactivation selon votre modèle
		// user.setActive(false);
		userRepository.save(user);
		
		// Invalider le cache
		invalidateUserCache();
		System.out.println("🗑️ Cache invalidé après désactivation utilisateur");
	}

	/**
	 * Active un utilisateur ET invalide le cache
	 */
	public void activateUser(Integer userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));
		// Implémentez la logique d'activation selon votre modèle
		// user.setActive(true);
		userRepository.save(user);
		
		// Invalider le cache
		invalidateUserCache();
		System.out.println("🗑️ Cache invalidé après activation utilisateur");
	}

	/**
	 * Supprime un utilisateur ET invalide le cache
	 */
	public void deleteUser(Integer userId) {
		if (!userRepository.existsById(userId)) {
			throw new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId);
		}
		userRepository.deleteById(userId);
		
		// Invalider le cache
		invalidateUserCache();
		System.out.println("🗑️ Cache invalidé après suppression utilisateur");
	}

	/**
	 * Recherche d'utilisateurs (sans cache car recherche dynamique)
	 */
	public List<UserDTO> searchUsers(String query, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<User> userPage = userRepository.findByEmailContainingOrFirstNameContainingOrLastNameContaining(query,
				query, query, pageable);
		return userPage.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	/**
	 * Invalide le cache des utilisateurs
	 */
	public void invalidateUserCache() {
		adminStatsCacheService.invalidateAllAdminCache();
	}

	private UserDTO convertToDTO(User user) {
		UserDTO dto = new UserDTO();
		dto.setId(user.getId());
		dto.setUsername(user.getUsername());
		dto.setEmail(user.getEmail());
		dto.setRole(user.getRole().name());
		dto.setFirstname(user.getFirstName());
		dto.setLastname(user.getLastName());
		dto.setEmail(user.getEmail());
		dto.setRole(user.getRole().name());
		return dto;
	}
}