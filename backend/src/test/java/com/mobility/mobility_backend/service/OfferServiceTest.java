package com.mobility.mobility_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

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

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("ci")
public class OfferServiceTest {

	@Mock
	private OfferRepository offerRepository;

	@Mock
	private OfferMapper offerMapper;

	@Mock
	private CityRepository cityRepository;

	@Mock
	private MobilityServiceRepository mobilityServiceRepository;

	@Mock
	private AdminRepository adminRepository;

	@InjectMocks
	private OfferService offerService;

	/*
	 * ce test simule la récupération de toutes les offres, leur conversion en DTOs,
	 * et confirme que le service fonctionne correctement en renvoyant la liste
	 * attendue et en appelant la bonne méthode du repository.
	 */
	@Test
	void whenGetAllOffers_thenReturnOfferList() {
		// Given
		Offer offer1 = new Offer();
		Offer offer2 = new Offer();
		List<Offer> offers = Arrays.asList(offer1, offer2);

		OfferDTO dto1 = new OfferDTO();
		OfferDTO dto2 = new OfferDTO();

		when(offerRepository.findAll()).thenReturn(offers);
		when(offerMapper.toDTO(offer1)).thenReturn(dto1);
		when(offerMapper.toDTO(offer2)).thenReturn(dto2);

		// When
		List<OfferDTO> result = offerService.getAllOffers();

		// Then
		assertThat(result).hasSize(2);
		verify(offerRepository, times(1)).findAll();
	}

	/*
	 * Ce test simule la récupération d'une offre existante par son ID, sa
	 * conversion en DTO, et confirme que le service retourne correctement
	 * l'OfferDTO correspondant dans un Optional, validant ainsi le bon
	 * fonctionnement de la logique.
	 */
	@Test
	void whenGetExistingOfferById_thenReturnOffer() {
		// Given
		Integer offerId = 1;
		Offer offer = new Offer();
		OfferDTO offerDTO = new OfferDTO();

		when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));
		when(offerMapper.toDTO(offer)).thenReturn(offerDTO);

		// When
		Optional<OfferDTO> result = offerService.getOfferById(offerId);

		// Then
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(offerDTO);
	}

	/*
	 * Ce test simule une recherche d'une offre inexistante par son ID et confirme
	 * que le service gère correctement ce cas en renvoyant un Optional vide,
	 * validant ainsi la gestion des erreurs ou des cas d'absence de données.
	 */

	@Test
	void whenGetNonExistingOfferById_thenReturnEmpty() {
		// Given
		Integer offerId = 999;
		when(offerRepository.findById(offerId)).thenReturn(Optional.empty());

		// When
		Optional<OfferDTO> result = offerService.getOfferById(offerId);

		// Then
		assertThat(result).isEmpty();
	}

	/*
	 * CORRECTION : Ce test simule la création d'une offre valide avec
	 * CreateOfferDTO
	 */
	@Test
	void whenCreateValidOffer_thenOfferIsSaved() {
		// Given
		CreateOfferDTO createOfferDTO = createValidCreateOfferDTO();
		OfferDTO offerDTO = createValidOfferDTO();
		Offer offer = new Offer();
		Offer savedOffer = new Offer();

		// Mock des repositories pour les relations
		City pickupCity = new City();
		City returnCity = new City();
		MobilityService mobilityService = new MobilityService();
		Admin admin = new Admin();

		when(cityRepository.findById(1)).thenReturn(Optional.of(pickupCity));
		when(cityRepository.findById(2)).thenReturn(Optional.of(returnCity));
		when(mobilityServiceRepository.findById(1)).thenReturn(Optional.of(mobilityService));
		when(adminRepository.findAll()).thenReturn(List.of(admin));

		when(offerRepository.save(any(Offer.class))).thenReturn(savedOffer);
		when(offerMapper.toDTO(savedOffer)).thenReturn(offerDTO);

		// When
		OfferDTO result = offerService.createOffer(createOfferDTO);

		// Then
		assertThat(result).isEqualTo(offerDTO);
		verify(offerRepository, times(1)).save(any(Offer.class));
	}

	/*
	 * CORRECTION : Ce test simule la mise à jour d'une offre existante
	 */
	@Test
	void whenUpdateExistingOffer_thenOfferIsUpdated() {
		// Given
		Integer offerId = 1;
		OfferDTO offerDTO = createValidOfferDTO();
		Offer existingOffer = new Offer();
		Offer updatedOffer = new Offer();

		when(offerRepository.findById(offerId)).thenReturn(Optional.of(existingOffer));
		when(offerRepository.save(any(Offer.class))).thenReturn(updatedOffer);
		when(offerMapper.toDTO(updatedOffer)).thenReturn(offerDTO);

		// When
		Optional<OfferDTO> result = offerService.updateOffer(offerId, offerDTO);

		// Then
		assertThat(result).isPresent();
		verify(offerRepository, times(1)).save(existingOffer);
	}

	/*
	 * ce test simule la suppression d'une offre existante, vérifie que le service
	 * retourne true pour indiquer une suppression réussie, et confirme que la
	 * méthode appropriée du repository est appelée, validant ainsi le flux de
	 * suppression.
	 */

	@Test
	void whenDeleteExistingOffer_thenReturnTrue() {
		// Given
		Integer offerId = 1;
		when(offerRepository.existsById(offerId)).thenReturn(true);

		// When
		boolean result = offerService.deleteOffer(offerId);

		// Then
		assertThat(result).isTrue();
		verify(offerRepository, times(1)).deleteById(offerId);
	}

	// CORRECTION : Ajoutez cette méthode pour CreateOfferDTO
	private CreateOfferDTO createValidCreateOfferDTO() {
		CreateOfferDTO dto = new CreateOfferDTO();
		dto.setPickupLocationName("paris");
		dto.setReturnLocationName("lyon");
		dto.setMobilityServiceId(null);
		dto.setPickupDatetime(LocalDateTime.now().plusDays(1));
		dto.setPrice(new BigDecimal("25.50"));
		dto.setDescription("Test offer");
		dto.setStatus(Offer.OfferStatus.PENDING);
		dto.setActive(true);
		return dto;
	}

	private OfferDTO createValidOfferDTO() {
		OfferDTO dto = new OfferDTO();
		dto.setPickupDatetime(LocalDateTime.now().plusDays(1));
		dto.setPrice(new BigDecimal("25.50"));
		dto.setDescription("Test offer");
		dto.setStatus(Offer.OfferStatus.PENDING);
		dto.setActive(true);
		return dto;
	}
}