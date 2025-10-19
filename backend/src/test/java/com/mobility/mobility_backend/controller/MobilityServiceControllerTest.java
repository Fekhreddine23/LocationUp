package com.mobility.mobility_backend.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobility.mobility_backend.dto.MobilityServiceDTO;
import com.mobility.mobility_backend.service.MobilityServiceService;

@WebMvcTest(MobilityServiceController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
		"spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration",
		"spring.security.enabled=false" })
class MobilityServiceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private MobilityServiceService mobilityServiceService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void whenCreateMobilityService_thenSuccess() throws Exception {
		// Given
		MobilityServiceDTO requestDTO = new MobilityServiceDTO(null, "Vélib", "Service de vélos");
		MobilityServiceDTO responseDTO = new MobilityServiceDTO(1, "Vélib", "Service de vélos");

		when(mobilityServiceService.createMobilityService(anyString(), anyString())).thenReturn(responseDTO);

		// When & Then
		mockMvc.perform(post("/api/mobility-services").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.serviceId").value(1L)).andExpect(jsonPath("$.name").value("Vélib"))
				.andExpect(jsonPath("$.description").value("Service de vélos"));
	}

	@Test
	void whenCreateMobilityServiceWithError_thenBadRequest() throws Exception {
		// Given
		MobilityServiceDTO requestDTO = new MobilityServiceDTO(null, "Vélib", "Service de vélos");

		when(mobilityServiceService.createMobilityService(anyString(), anyString()))
				.thenThrow(new RuntimeException("Service existe déjà"));

		// When & Then
		mockMvc.perform(post("/api/mobility-services").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDTO))).andExpect(status().isBadRequest());
	}

	@Test
	void whenGetAllMobilityServices_thenSuccess() throws Exception {
		// Given
		MobilityServiceDTO service1 = new MobilityServiceDTO(1, "Vélib", "Service de vélos");
		MobilityServiceDTO service2 = new MobilityServiceDTO(2, "RATP", "Transports parisiens");
		List<MobilityServiceDTO> services = Arrays.asList(service1, service2);

		when(mobilityServiceService.getAllMobilityServices()).thenReturn(services);

		// When & Then
		mockMvc.perform(get("/api/mobility-services")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].name").value("Vélib"))
				.andExpect(jsonPath("$[1].name").value("RATP"));
	}

	@Test
	void whenGetMobilityServiceById_thenSuccess() throws Exception {
		// Given
		MobilityServiceDTO service = new MobilityServiceDTO(1, "Vélib", "Service de vélos");

		when(mobilityServiceService.getMobilityServiceById(1)).thenReturn(Optional.of(service));

		// When & Then
		mockMvc.perform(get("/api/mobility-services/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.serviceId").value(1L)).andExpect(jsonPath("$.name").value("Vélib"));
	}

	@Test
	void whenGetNonExistingMobilityServiceById_thenNotFound() throws Exception {
		// Given
		when(mobilityServiceService.getMobilityServiceById(99)).thenReturn(Optional.empty());

		// When & Then
		mockMvc.perform(get("/api/mobility-services/99")).andExpect(status().isNotFound());
	}

	@Test
	void whenGetMobilityServiceByName_thenSuccess() throws Exception {
		// Given
		MobilityServiceDTO service = new MobilityServiceDTO(1, "Vélib", "Service de vélos");

		when(mobilityServiceService.getMobilityServiceByName("Vélib")).thenReturn(Optional.of(service));

		// When & Then
		mockMvc.perform(get("/api/mobility-services/name/Vélib")).andExpect(status().isOk())
				.andExpect(jsonPath("$.serviceId").value(1L)).andExpect(jsonPath("$.name").value("Vélib"));
	}

	@Test
	void whenGetNonExistingMobilityServiceByName_thenNotFound() throws Exception {
		// Given
		when(mobilityServiceService.getMobilityServiceByName("NonExistent")).thenReturn(Optional.empty());

		// When & Then
		mockMvc.perform(get("/api/mobility-services/name/NonExistent")).andExpect(status().isNotFound());
	}

	@Test
	void whenDeleteExistingMobilityService_thenSuccess() throws Exception {
		// Given
		when(mobilityServiceService.deleteMobilityService(1)).thenReturn(true);

		// When & Then
		mockMvc.perform(delete("/api/mobility-services/1")).andExpect(status().isNoContent());
	}

	@Test
	void whenDeleteNonExistingMobilityService_thenNotFound() throws Exception {
		// Given
		when(mobilityServiceService.deleteMobilityService(99)).thenReturn(false);

		// When & Then
		mockMvc.perform(delete("/api/mobility-services/99")).andExpect(status().isNotFound());
	}
}
