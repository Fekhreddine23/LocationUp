package com.mobility.mobility_backend.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobility.mobility_backend.entity.Admin;
import com.mobility.mobility_backend.entity.City;
import com.mobility.mobility_backend.entity.MobilityService;
import com.mobility.mobility_backend.entity.Offer;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Integer> {

	// Recherche basique
	Optional<Offer> findByOfferId(Integer offerId);

	// Recherche par service de mobilité
	List<Offer> findByMobilityService(MobilityService mobilityService);

	List<Offer> findByMobilityServiceServiceId(Integer serviceId);

	// Recherche par lieu de départ/retour
	List<Offer> findByPickupLocation(City pickupLocation);

	List<Offer> findByReturnLocation(City returnLocation);

	List<Offer> findByPickupLocationCityId(Integer cityId);

	List<Offer> findByReturnLocationCityId(Integer cityId);

	// Recherche par administrateur
	List<Offer> findByAdmin(Admin admin);

	List<Offer> findByAdminAdminId(Integer adminId);

	// Recherche par prix
	List<Offer> findByPriceLessThan(BigDecimal maxPrice);

	List<Offer> findByPriceGreaterThan(BigDecimal minPrice);

	List<Offer> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

	// Recherche par date
	List<Offer> findByPickupDatetimeAfter(LocalDateTime date);

	List<Offer> findByPickupDatetimeBefore(LocalDateTime date);

	List<Offer> findByPickupDatetimeBetween(LocalDateTime startDate, LocalDateTime endDate);

	// Recherche combinée
	List<Offer> findByPickupLocationAndReturnLocation(City pickupLocation, City returnLocation);

	List<Offer> findByPickupLocationCityIdAndReturnLocationCityId(Integer pickupCityId, Integer returnCityId);

	List<Offer> findByMobilityServiceAndPriceLessThan(MobilityService mobilityService, BigDecimal maxPrice);

	// Vérification d'existence
	boolean existsByOfferId(Integer offerId);

	boolean existsByPickupLocationAndPickupDatetime(City pickupLocation, LocalDateTime pickupDatetime);

	// Tri et pagination implicites
	List<Offer> findByOrderByPriceAsc();

	List<Offer> findByOrderByPriceDesc();

	List<Offer> findByOrderByPickupDatetimeAsc();

	List<Offer> findByOrderByPickupDatetimeDesc();

	List<Offer> findByOrderByCreatedAtDesc();

	// ✅ Méthodes pour les statistiques d'activation
	long countByActiveTrue();

	long countByActiveFalse();

	// ✅ Méthodes pour les statistiques de statut (si votre entité Offer a un champ
	// "status")
	long countByStatus(Offer.OfferStatus status);

	// ✅ Méthode pour pagination
	@Override
	Page<Offer> findAll(Pageable pageable);

}