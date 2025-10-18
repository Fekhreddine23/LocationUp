package com.mobility.mobility_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobility.mobility_backend.dto.OfferDTO;
import com.mobility.mobility_backend.service.OfferService;

@WebMvcTest(OfferController.class)
@TestPropertySource(properties = {
	    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration",
	    "spring.security.enabled=false"
	})
public class OfferControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private OfferService offerService;

	@Autowired
	private ObjectMapper objectMapper;

	/*
	 * ce test simule une requête pour récupérer toutes les offres via l'API,
	 * configure le service pour retourner deux offres prédéfinies, et vérifie que
	 * la réponse HTTP est correcte (statut 200) et contient les deux offres avec
	 * leurs offerId respectifs, validant ainsi le bon fonctionnement de l'endpoint.
	 * L'utilisation de MockMvc permet de tester le contrôleur sans démarrer un
	 * serveur réel.
	 */
	@Test
	void whenGetAllOffers_thenReturnOffers() throws Exception {
		// Given
		OfferDTO offer1 = createOfferDTO(1);
		OfferDTO offer2 = createOfferDTO(2);
		when(offerService.getAllOffers()).thenReturn(Arrays.asList(offer1, offer2));

		// When & Then
		mockMvc.perform(get("/api/offers")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].offerId").value(1)).andExpect(jsonPath("$[1].offerId").value(2));
	}

	/*
	 * ce test simule la création d'une offre valide via l'API, configure le service
	 * pour retourner une offre avec un ID attribué, et vérifie que la réponse HTTP
	 * est correcte (statut 201) avec l'offerId attendu, validant ainsi le bon
	 * fonctionnement de l'endpoint de création. L'utilisation de MockMvc permet de
	 * tester le contrôleur dans un environnement simulé.
	 */

	@Test
	void whenCreateValidOffer_thenReturnCreated() throws Exception {
		// Given
		OfferDTO offerDTO = createOfferDTO(null);
		OfferDTO createdOffer = createOfferDTO(1);

		when(offerService.createOffer(any(OfferDTO.class))).thenReturn(createdOffer);

		// When & Then
		mockMvc.perform(post("/api/offers").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(offerDTO))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.offerId").value(1));
	}

	/*
	 * , ce test simule la mise à jour d'une offre existante avec l'ID 1 via l'API,
	 * configure le service pour retourner une offre mise à jour avec cet ID, et
	 * vérifie que la réponse HTTP est correcte (statut 200) avec l'offerId attendu,
	 * validant ainsi le bon fonctionnement de l'endpoint de mise à jour.
	 * L'utilisation de MockMvc permet de tester le contrôleur dans un environnement
	 * simulé.
	 */
	@Test
	void whenUpdateExistingOffer_thenReturnUpdatedOffer() throws Exception {
		// Given
		OfferDTO offerDTO = createOfferDTO(null);
		OfferDTO updatedOffer = createOfferDTO(1);

		when(offerService.updateOffer(eq(1), any(OfferDTO.class))).thenReturn(Optional.of(updatedOffer));

		// When & Then
		mockMvc.perform(put("/api/offers/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(offerDTO))).andExpect(status().isOk())
				.andExpect(jsonPath("$.offerId").value(1));
	}

	/*
	 * ce test simule la suppression d'une offre existante avec l'ID 1 via l'API,
	 * configure le service pour indiquer une suppression réussie, et vérifie que la
	 * réponse HTTP retourne un statut 204 (No Content), validant ainsi le bon
	 * fonctionnement de l'endpoint de suppression. L'utilisation de MockMvc permet
	 * de tester le contrôleur dans un environnement simulé
	 */
	@Test
	void whenDeleteExistingOffer_thenReturnNoContent() throws Exception {
		// Given
		when(offerService.deleteOffer(1)).thenReturn(true);

		// When & Then
		mockMvc.perform(delete("/api/offers/1")).andExpect(status().isNoContent());
	}

	private OfferDTO createOfferDTO(Integer id) {
		OfferDTO dto = new OfferDTO();
		dto.setOfferId(id);
		dto.setPickupDatetime(LocalDateTime.now().plusDays(1));
		dto.setPrice(new BigDecimal("25.50"));
		dto.setDescription("Test offer");
		return dto;
	}

}
