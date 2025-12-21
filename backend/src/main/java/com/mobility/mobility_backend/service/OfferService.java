package com.mobility.mobility_backend.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobility.mobility_backend.dto.CreateOfferDTO;
import com.mobility.mobility_backend.dto.OfferDTO;
import com.mobility.mobility_backend.dto.OfferMapper;
import com.mobility.mobility_backend.entity.Admin;
import com.mobility.mobility_backend.entity.City;
import com.mobility.mobility_backend.entity.MobilityService;
import com.mobility.mobility_backend.entity.Offer;
import com.mobility.mobility_backend.repository.AdminRepository;
import com.mobility.mobility_backend.repository.CityRepository;
import com.mobility.mobility_backend.repository.MobilityServiceRepository;
import com.mobility.mobility_backend.repository.OfferRepository;
import com.mobility.mobility_backend.util.SanitizationUtils;

@Service
@Transactional
public class OfferService {

	private final OfferRepository offerRepository;
	private final OfferMapper offerMapper;
	private final CityRepository cityRepository;
	private final MobilityServiceRepository mobilityServiceRepository;
	private final AdminRepository adminRepository;

	@Autowired
	public OfferService(OfferRepository offerRepository, OfferMapper offerMapper, CityRepository cityRepository,
			MobilityServiceRepository mobilityServiceRepository, AdminRepository adminRepository) {
		this.offerRepository = offerRepository;
		this.offerMapper = offerMapper;
		this.cityRepository = cityRepository;
		this.mobilityServiceRepository = mobilityServiceRepository;
		this.adminRepository = adminRepository;
	}

	@Transactional(readOnly = true)
	public List<OfferDTO> getAllOffers() {
		return offerRepository.findAll().stream().map(offerMapper::toDTO).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public Optional<OfferDTO> getOfferById(Integer id) {
		return offerRepository.findById(id).map(offerMapper::toDTO);
	}

	public OfferDTO createOffer(CreateOfferDTO createOfferDTO) {
		System.out.println("➕ Début création offre");
		System.out.println("📍 pickupLocationId: " + createOfferDTO.getPickupLocationName());
		System.out.println("📍 returnLocationId: " + createOfferDTO.getReturnLocationName());
		System.out.println("🚗 mobilityServiceId: " + createOfferDTO.getMobilityServiceId());
		System.out.println("📅 pickupDatetime: " + createOfferDTO.getPickupDatetime());
		System.out.println("💰 price: " + createOfferDTO.getPrice());
		System.out.println("📝 description: " + createOfferDTO.getDescription());
		System.out.println("📊 status: " + createOfferDTO.getStatus());
		System.out.println("🔘 active: " + createOfferDTO.isActive());

		try {

			if (createOfferDTO.getPickupLocationName() == null) {
				throw new IllegalArgumentException("La ville du lieu de départ est requis");
			}
			if (createOfferDTO.getReturnLocationName() == null) {
				throw new IllegalArgumentException("La ville du lieu de retour est requis");
			}
			if (createOfferDTO.getMobilityServiceId() == null) {
				throw new IllegalArgumentException("Le nom du service de mobilité est requis");
			}

			validateCreateOffer(createOfferDTO);

			// CHARGEMENT DES ENTITÉS LIÉES
			System.out.println("📥 Chargement des entités liées...");

			String sanitizedPickupName = SanitizationUtils.sanitizeText(createOfferDTO.getPickupLocationName());
			String sanitizedReturnName = SanitizationUtils.sanitizeText(createOfferDTO.getReturnLocationName());
			if (sanitizedPickupName.isBlank() || sanitizedReturnName.isBlank()) {
				throw new IllegalArgumentException("Les lieux de départ/retour ne peuvent pas être vides");
			}

			City pickupLocation = cityRepository.findByName(sanitizedPickupName).orElseGet(() -> {
				// Crée la ville si elle n'existe pas
				City newCity = new City();
				newCity.setName(sanitizedPickupName);
				System.out.println("➕ Création nouvelle ville: " + newCity.getName());
				return cityRepository.save(newCity);
			});

			City returnLocation = cityRepository.findByName(sanitizedReturnName).orElseGet(() -> {
				City newCity = new City();
				newCity.setName(sanitizedReturnName);
				return cityRepository.save(newCity);
			});

			MobilityService mobilityService = mobilityServiceRepository.findById(createOfferDTO.getMobilityServiceId())
					.orElseThrow(() -> {
						System.out
								.println("❌ Service mobilité non trouvé ID: " + createOfferDTO.getMobilityServiceId());
						return new RuntimeException(
								"Service de mobilité non trouvé avec l'ID: " + createOfferDTO.getMobilityServiceId());
					});
			System.out.println("✅ Service mobilité: " + mobilityService.getName());

			// RÉCUPÉRATION DE L'ADMIN
			Admin admin = getCurrentAdmin();
			System.out.println("✅ Admin assigné: " + admin.getUsername() + " (ID: " + admin.getAdminId() + ")");

			// CRÉATION DE L'OFFRE
			Offer offer = new Offer();
			offer.setPickupLocation(pickupLocation);
			offer.setReturnLocation(returnLocation);
			offer.setMobilityService(mobilityService);
			offer.setAdmin(admin);
			offer.setPickupDatetime(createOfferDTO.getPickupDatetime());
			offer.setDescription(SanitizationUtils.sanitizeText(createOfferDTO.getDescription()));
			offer.setPrice(createOfferDTO.getPrice());
			offer.setStatus(createOfferDTO.getStatus());
			offer.setActive(createOfferDTO.isActive());
			offer.setImageUrl(createOfferDTO.getImageUrl());
			offer.setGalleryUrls(formatGalleryUrls(createOfferDTO.getGalleryUrls()));

			System.out.println("💾 Sauvegarde de l'offre...");
			Offer savedOffer = offerRepository.save(offer);
			System.out.println("✅ Offre créée avec ID: " + savedOffer.getOfferId());

			return offerMapper.toDTO(savedOffer);

		} catch (Exception e) {
			System.out.println("❌ Erreur création offre: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Erreur lors de la création de l'offre: " + e.getMessage(), e);
		}
	}

	private Admin getCurrentAdmin() {
		try {
			System.out.println("🔍 Recherche d'un administrateur...");

			// Récupérer l'admin connecté depuis le contexte de sécurité
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			if (authentication != null && authentication.isAuthenticated()
					&& !"anonymousUser".equals(authentication.getPrincipal())) {

				String username = authentication.getName();
				System.out.println("👤 Admin connecté: " + username);

				Optional<Admin> adminOpt = adminRepository.findByUsername(username);
				if (adminOpt.isPresent()) {
					Admin admin = adminOpt.get();
					System.out.println("✅ Admin trouvé: " + admin.getUsername() + " (ID: " + admin.getAdminId() + ")");
					return admin;
				}
			}

			throw new RuntimeException("❌ Aucun administrateur authentifié dans le contexte de sécurité");

		} catch (Exception e) {
			System.out.println("❌ Erreur dans getCurrentAdmin: " + e.getMessage());
			throw new RuntimeException("Impossible de récupérer l'administrateur: " + e.getMessage(), e);
		}
	}

	private void validateCreateOffer(CreateOfferDTO createOfferDTO) {
		if (createOfferDTO.getPrice() != null && createOfferDTO.getPrice().signum() <= 0) {
			throw new IllegalArgumentException("Le prix doit être positif");
		}
		if (createOfferDTO.getPickupDatetime() != null
				&& createOfferDTO.getPickupDatetime().isBefore(java.time.LocalDateTime.now())) {
			throw new IllegalArgumentException("La date de pickup doit être dans le futur");
		}
	}

	public Optional<OfferDTO> updateOffer(Integer id, OfferDTO offerDTO) {
		System.out.println("🔄 Début mise à jour offre ID: " + id);
		System.out.println("📦 Données reçues: " + offerDTO.toString());

		Optional<Offer> existingOfferOpt = offerRepository.findById(id);
		if (!existingOfferOpt.isPresent()) {
			System.out.println("❌ Offre non trouvée ID: " + id);
			return Optional.empty();
		}

		Offer existingOffer = existingOfferOpt.get();
		validateOffer(offerDTO);

		// 🎯 MISE À JOUR DES VILLES (CORRECTION CRITIQUE)
		if (offerDTO.getPickupLocationName() != null
				&& !offerDTO.getPickupLocationName().equals(existingOffer.getPickupLocation().getName())) {

			System.out.println("📍 Mise à jour ville départ: " + offerDTO.getPickupLocationName());
			City pickupCity = cityRepository.findByName(offerDTO.getPickupLocationName()).orElseGet(() -> {
				City newCity = new City();
				newCity.setName(offerDTO.getPickupLocationName());
				newCity.setPostalCode("NC");
				System.out.println("➕ Création nouvelle ville départ: " + newCity.getName());
				return cityRepository.save(newCity);
			});
			existingOffer.setPickupLocation(pickupCity);
		}

		if (offerDTO.getReturnLocationName() != null
				&& !offerDTO.getReturnLocationName().equals(existingOffer.getReturnLocation().getName())) {

			System.out.println("📍 Mise à jour ville retour: " + offerDTO.getReturnLocationName());
			City returnCity = cityRepository.findByName(offerDTO.getReturnLocationName()).orElseGet(() -> {
				City newCity = new City();
				newCity.setName(offerDTO.getReturnLocationName());
				newCity.setPostalCode("NC");
				System.out.println("➕ Création nouvelle ville retour: " + newCity.getName());
				return cityRepository.save(newCity);
			});
			existingOffer.setReturnLocation(returnCity);
		}

		// 🎯 MISE À JOUR DU SERVICE DE MOBILITÉ
		if (offerDTO.getMobilityServiceId() != null) {
			MobilityService mobilityService = mobilityServiceRepository.findById(offerDTO.getMobilityServiceId())
					.orElseThrow(() -> new RuntimeException("Service de mobilité non trouvé"));
			existingOffer.setMobilityService(mobilityService);
			System.out.println("🚗 Service mobilité mis à jour ID: " + offerDTO.getMobilityServiceId());
		}

		// Mettre à jour les autres champs
		if (offerDTO.getDescription() != null) {
			existingOffer.setDescription(SanitizationUtils.sanitizeText(offerDTO.getDescription()));
		}
		if (offerDTO.getPrice() != null) {
			existingOffer.setPrice(offerDTO.getPrice());
		}
		if (offerDTO.getPickupDatetime() != null) {
			existingOffer.setPickupDatetime(offerDTO.getPickupDatetime());
		}
		if (offerDTO.getStatus() != null) {
			existingOffer.setStatus(offerDTO.getStatus());
		}
		if (offerDTO.getImageUrl() != null) {
			existingOffer.setImageUrl(offerDTO.getImageUrl());
		}
		if (offerDTO.getGalleryUrls() != null) {
			existingOffer.setGalleryUrls(formatGalleryUrls(offerDTO.getGalleryUrls()));
		}

		existingOffer.setActive(offerDTO.isActive());

		System.out.println("💾 Sauvegarde de l'offre mise à jour...");
		Offer updatedOffer = offerRepository.save(existingOffer);
		System.out.println("✅ Offre mise à jour ID: " + updatedOffer.getOfferId());

		return Optional.of(offerMapper.toDTO(updatedOffer));
	}

	private String formatGalleryUrls(java.util.List<String> urls) {
		if (urls == null || urls.isEmpty()) {
			return null;
		}
		final int MAX_IMAGES = 5;
		java.util.List<String> filtered = urls.stream()
				.filter(u -> u != null && !u.isBlank())
				.map(String::trim)
				.filter(this::isSafeImageUrl)
				.limit(MAX_IMAGES)
				.toList();
		if (filtered.isEmpty()) {
			return null;
		}
		try {
			return com.mobility.mobility_backend.dto.OfferMapper.OBJECT_MAPPER.writeValueAsString(filtered);
		} catch (Exception e) {
			return String.join(",", filtered);
		}
	}

	private boolean isSafeImageUrl(String url) {
		if (url.startsWith("/uploads/")) return true;
		return url.startsWith("http://") || url.startsWith("https://");
	}

	public boolean deleteOffer(Integer id) {
		if (offerRepository.existsById(id)) {
			offerRepository.deleteById(id);
			return true;
		}
		return false;
	}

	private void validateOffer(OfferDTO offerDTO) {
		if (offerDTO.getPrice() != null && offerDTO.getPrice().signum() <= 0) {
			throw new IllegalArgumentException("Le prix doit être positif");
		}
	}

	public Page<OfferDTO> getAllOffers(Pageable pageable) {
		Page<Offer> offerPage = offerRepository.findAll(pageable);
		return offerPage.map(offerMapper::toDTO);
	}

	public Object getOffersStats() {
		System.out.println("📊 Calculating offers stats...");

		long totalOffers = offerRepository.count();
		System.out.println("📈 Total offers: " + totalOffers);

		long pendingOffers = offerRepository.countByStatus(Offer.OfferStatus.PENDING);
		long confirmedOffers = offerRepository.countByStatus(Offer.OfferStatus.CONFIRMED);
		long cancelledOffers = offerRepository.countByStatus(Offer.OfferStatus.CANCELLED);

		System.out.println("📈 Pending: " + pendingOffers);
		System.out.println("📈 Confirmed: " + confirmedOffers);
		System.out.println("📈 Cancelled: " + cancelledOffers);

		Map<String, Object> stats = new HashMap<>();
		stats.put("total", totalOffers);
		stats.put("pending", pendingOffers);
		stats.put("confirmed", confirmedOffers);
		stats.put("cancelled", cancelledOffers);

		double confirmationRate = totalOffers > 0 ? (confirmedOffers * 100.0 / totalOffers) : 0;
		stats.put("confirmationRate", Math.round(confirmationRate * 100.0) / 100.0);

		System.out.println("📊 Final stats: " + stats);
		return stats;
	}

	public OfferDTO updateOfferStatus(Integer offerId, boolean active) {
		Optional<Offer> offerOpt = offerRepository.findById(offerId);
		if (offerOpt.isPresent()) {
			Offer offer = offerOpt.get();
			offer.setActive(active);
			Offer savedOffer = offerRepository.save(offer);
			return offerMapper.toDTO(savedOffer);
		} else {
			throw new RuntimeException("Offre non trouvée avec l'ID: " + offerId);
		}
	}

}
