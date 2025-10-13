package com.mobility.mobility_backend.service;

import com.mobility.mobility_backend.dto.MobilityServiceDTO;
import com.mobility.mobility_backend.entity.MobilityService;
import com.mobility.mobility_backend.repository.MobilityServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("ci")
class MobilityServiceServiceImplTest {

	@Mock
	private MobilityServiceRepository mobilityServiceRepository;

	@InjectMocks
	private MobilityServiceServiceImpl mobilityServiceService;

	private MobilityService velibService;
	private MobilityService ratpService;

	@BeforeEach
	void setUp() {
		velibService = new MobilityService("Vélib", "Service de vélos en libre-service");
		velibService.setServiceId(1L);

		ratpService = new MobilityService("RATP", "Régie autonome des transports parisiens");
		ratpService.setServiceId(2L);
	}

	@Test
	void whenCreateMobilityService_thenSuccess() {
		// Given
		when(mobilityServiceRepository.existsByName("Vélib")).thenReturn(false);
		when(mobilityServiceRepository.save(any(MobilityService.class))).thenReturn(velibService);

		// When
		MobilityServiceDTO result = mobilityServiceService.createMobilityService("Vélib",
				"Service de vélos en libre-service");

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getServiceId()).isEqualTo(1L);
		assertThat(result.getName()).isEqualTo("Vélib");
		assertThat(result.getDescription()).isEqualTo("Service de vélos en libre-service");

		verify(mobilityServiceRepository).existsByName("Vélib");
		verify(mobilityServiceRepository).save(any(MobilityService.class));
	}

	@Test
	void whenCreateExistingMobilityService_thenThrowException() {
		// Given
		when(mobilityServiceRepository.existsByName("Vélib")).thenReturn(true);

		// When & Then
		assertThatThrownBy(() -> mobilityServiceService.createMobilityService("Vélib", "Description"))
				.isInstanceOf(RuntimeException.class).hasMessageContaining("existe déjà");

		verify(mobilityServiceRepository, never()).save(any(MobilityService.class));
	}

	@Test
	void whenGetMobilityServiceById_thenSuccess() {
		// Given
		when(mobilityServiceRepository.findById(1L)).thenReturn(Optional.of(velibService));

		// When
		Optional<MobilityServiceDTO> result = mobilityServiceService.getMobilityServiceById(1L);

		// Then
		assertThat(result).isPresent();
		assertThat(result.get().getName()).isEqualTo("Vélib");
		verify(mobilityServiceRepository).findById(1L);
	}

	@Test
	void whenGetNonExistingMobilityServiceById_thenReturnEmpty() {
		// Given
		when(mobilityServiceRepository.findById(99L)).thenReturn(Optional.empty());

		// When
		Optional<MobilityServiceDTO> result = mobilityServiceService.getMobilityServiceById(99L);

		// Then
		assertThat(result).isEmpty();
		verify(mobilityServiceRepository).findById(99L);
	}

	@Test
	void whenGetMobilityServiceByName_thenSuccess() {
		// Given
		when(mobilityServiceRepository.findByName("Vélib")).thenReturn(Optional.of(velibService));

		// When
		Optional<MobilityServiceDTO> result = mobilityServiceService.getMobilityServiceByName("Vélib");

		// Then
		assertThat(result).isPresent();
		assertThat(result.get().getName()).isEqualTo("Vélib");
		verify(mobilityServiceRepository).findByName("Vélib");
	}

	@Test
	void whenGetAllMobilityServices_thenSuccess() {
		// Given
		List<MobilityService> services = Arrays.asList(velibService, ratpService);
		when(mobilityServiceRepository.findAll()).thenReturn(services);

		// When
		List<MobilityServiceDTO> result = mobilityServiceService.getAllMobilityServices();

		// Then
		assertThat(result).hasSize(2);
		assertThat(result.get(0).getName()).isEqualTo("Vélib");
		assertThat(result.get(1).getName()).isEqualTo("RATP");
		verify(mobilityServiceRepository).findAll();
	}

	@Test
	void whenMobilityServiceExists_thenReturnTrue() {
		// Given
		when(mobilityServiceRepository.existsByName("Vélib")).thenReturn(true);

		// When
		boolean result = mobilityServiceService.mobilityServiceExists("Vélib");

		// Then
		assertThat(result).isTrue();
		verify(mobilityServiceRepository).existsByName("Vélib");
	}

	@Test
	void whenDeleteExistingMobilityService_thenSuccess() {
		// Given
		when(mobilityServiceRepository.existsById(1L)).thenReturn(true);

		// When
		boolean result = mobilityServiceService.deleteMobilityService(1L);

		// Then
		assertThat(result).isTrue();
		verify(mobilityServiceRepository).deleteById(1L);
	}

	@Test
	void whenDeleteNonExistingMobilityService_thenReturnFalse() {
		// Given
		when(mobilityServiceRepository.existsById(99L)).thenReturn(false);

		// When
		boolean result = mobilityServiceService.deleteMobilityService(99L);

		// Then
		assertThat(result).isFalse();
		verify(mobilityServiceRepository, never()).deleteById(anyLong());
	}
}