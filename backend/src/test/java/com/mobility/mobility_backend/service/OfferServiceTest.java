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

import com.mobility.mobility_backend.dto.OfferDTO;
import com.mobility.mobility_backend.dto.OfferMapper;
import com.mobility.mobility_backend.entity.Offer;
import com.mobility.mobility_backend.repository.OfferRepository;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("ci")
public class OfferServiceTest {

	@Mock
	private OfferRepository offerRepository;

	@Mock
	private OfferMapper offerMapper;

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
	 * En résumé, ce test simule la création d'une offre valide, vérifie que
	 * l'entité est correctement sauvegardée via le repository et mappée en DTO, et
	 * confirme que le service retourne les données attendues, validant ainsi le
	 * flux de création.
	 */
	@Test
	void whenCreateValidOffer_thenOfferIsSaved() {
		// Given
		OfferDTO offerDTO = createValidOfferDTO();
		Offer offer = new Offer();
		Offer savedOffer = new Offer();

		when(offerMapper.toEntity(offerDTO)).thenReturn(offer);
		when(offerRepository.save(offer)).thenReturn(savedOffer);
		when(offerMapper.toDTO(savedOffer)).thenReturn(offerDTO);

		// When
		OfferDTO result = offerService.createOffer(offerDTO);

		// Then
		assertThat(result).isEqualTo(offerDTO);
		verify(offerRepository, times(1)).save(offer);
	}

	/*
	 * ce test simule la mise à jour d'une offre existante, vérifie que l'entité est
	 * correctement sauvegardée via le repository et mappée en DTO, et confirme que
	 * le service retourne un Optional contenant les données mises à jour, validant
	 * ainsi le flux de mise à jour.
	 */
	@Test
	void whenUpdateExistingOffer_thenOfferIsUpdated() {
		// Given
		Integer offerId = 1;
		OfferDTO offerDTO = createValidOfferDTO();
		Offer offer = new Offer();
		Offer updatedOffer = new Offer();

		when(offerRepository.existsById(offerId)).thenReturn(true);
		when(offerMapper.toEntity(offerDTO)).thenReturn(offer);
		when(offerRepository.save(any(Offer.class))).thenReturn(updatedOffer);
		when(offerMapper.toDTO(updatedOffer)).thenReturn(offerDTO);

		// When
		Optional<OfferDTO> result = offerService.updateOffer(offerId, offerDTO);

		// Then
		assertThat(result).isPresent();
		verify(offerRepository, times(1)).save(any(Offer.class));
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

	private OfferDTO createValidOfferDTO() {
		OfferDTO dto = new OfferDTO();
		dto.setPickupDatetime(LocalDateTime.now().plusDays(1));
		dto.setPrice(new BigDecimal("25.50"));
		dto.setDescription("Test offer");
		return dto;
	}

}
