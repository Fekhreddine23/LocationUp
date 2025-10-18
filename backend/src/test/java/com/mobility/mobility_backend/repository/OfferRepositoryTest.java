package com.mobility.mobility_backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.mobility.mobility_backend.entity.Admin;
import com.mobility.mobility_backend.entity.City;
import com.mobility.mobility_backend.entity.MobilityService;
import com.mobility.mobility_backend.entity.Offer;

@DataJpaTest
@ActiveProfiles("test")
public class OfferRepositoryTest {

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private  MobilityServiceRepository mobilityServiceRepository;

    // Méthode utilitaire pour créer des données UNIQUES
    private String generateUniqueString(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private Admin createUniqueAdmin() {
        String uniqueUsername = generateUniqueString("admin");
        String uniqueEmail = generateUniqueString("test") + "@test.com";
        
        Admin admin = new Admin(uniqueUsername, uniqueEmail, "password", 1);
        return entityManager.persistAndFlush(admin);
    }

    private City createUniqueCity(String namePrefix) {
        String uniqueName = generateUniqueString(namePrefix);
        City city = new City(uniqueName);
        return entityManager.persistAndFlush(city);
    }

    private MobilityService createUniqueMobilityService(String namePrefix) {
        String uniqueName = generateUniqueString(namePrefix);
        String uniqueDescription = generateUniqueString("desc");
        MobilityService service = new MobilityService(uniqueName, uniqueDescription);
        return entityManager.persistAndFlush(service);
    }

    private Offer createTestOffer() {
        City pickupCity = createUniqueCity("pickup");
        City returnCity = createUniqueCity("return");
        MobilityService service = createUniqueMobilityService("service");
        Admin admin = createUniqueAdmin();

        return Offer.builder()
            .pickupLocation(pickupCity)
            .returnLocation(returnCity)
            .mobilityService(service)
            .admin(admin)
            .pickupDatetime(LocalDateTime.now().plusDays(1))
            .description("Test Description")
            .price(new BigDecimal("15.99"))
            .build();
    }

    // TESTS CORRIGÉS

    @Test
    void whenSaveOffer_thenOfferIsSaved() {
        // Given
        Offer offer = createTestOffer();

        // When
        Offer savedOffer = offerRepository.save(offer);

        // Then
        assertThat(savedOffer).isNotNull();
        assertThat(savedOffer.getOfferId()).isNotNull();
        assertThat(savedOffer.getDescription()).isEqualTo("Test Description");
    }

    @Test
    void whenFindById_thenReturnOffer() {
        // Given
        Offer offer = createTestOffer();
        Offer savedOffer = entityManager.persistAndFlush(offer);

        // When
        Optional<Offer> foundOffer = offerRepository.findById(savedOffer.getOfferId());

        // Then
        assertThat(foundOffer).isPresent();
        assertThat(foundOffer.get().getOfferId()).isEqualTo(savedOffer.getOfferId());
    }

    @Test
    void whenFindAll_thenReturnAllOffers() {
        // Given
        Offer offer1 = createTestOffer();
        Offer offer2 = createTestOffer();
        entityManager.persistAndFlush(offer1);
        entityManager.persistAndFlush(offer2);

        // When
        List<Offer> offers = offerRepository.findAll();

        // Then
        assertThat(offers).hasSize(2);
    }

    @Test
    void whenFindByPickupLocation_thenReturnOffers() {
        // Given
        City paris = createUniqueCity("Paris");
        Offer offer = createTestOffer();
        offer.setPickupLocation(paris);
        entityManager.persistAndFlush(offer);

        // When
        List<Offer> offers = offerRepository.findByPickupLocation(paris);

        // Then
        assertThat(offers).hasSize(1);
        assertThat(offers.get(0).getPickupLocation().getName()).contains("Paris");
    }

    @Test
    void whenDeleteOffer_thenOfferIsDeleted() {
        // Given
        Offer offer = createTestOffer();
        Offer savedOffer = entityManager.persistAndFlush(offer);

        // When
        offerRepository.deleteById(savedOffer.getOfferId());
        entityManager.flush();

        // Then
        Optional<Offer> deletedOffer = offerRepository.findById(savedOffer.getOfferId());
        assertThat(deletedOffer).isEmpty();
    }

    @Test
    void whenFindMobilityService_thenDescriptionIsPresent() {
        // Given
        MobilityService service = createUniqueMobilityService("TestService");
        
        // When
        Optional<MobilityService> foundService = mobilityServiceRepository.findById(service.getServiceId());

        // Then
        assertThat(foundService).isPresent();
        assertThat(foundService.get().getDescription()).isNotNull();
    }

    @Test
    void whenCreateOfferWithMobilityService_thenDescriptionIsAccessible() {
        // Given
        MobilityService bikeService = createUniqueMobilityService("BikeService");
        Offer offer = createTestOffer();
        offer.setMobilityService(bikeService);
        Offer savedOffer = entityManager.persistAndFlush(offer);

        // When - Recharge depuis la base
        entityManager.clear();
        Offer refreshedOffer = offerRepository.findById(savedOffer.getOfferId()).orElseThrow();

        // Then
        assertThat(refreshedOffer.getMobilityService().getDescription()).isNotNull();
    }
}