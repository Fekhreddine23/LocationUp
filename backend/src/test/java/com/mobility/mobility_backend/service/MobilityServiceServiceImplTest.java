package com.mobility.mobility_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mobility.mobility_backend.dto.MobilityServiceDTO;
import com.mobility.mobility_backend.entity.MobilityService;
import com.mobility.mobility_backend.repository.MobilityServiceRepository;

import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
class MobilityServiceServiceImplTest {

	@Mock
	private MobilityServiceRepository mobilityServiceRepository;

	@InjectMocks
	private MobilityServiceServiceImpl mobilityServiceService;

	@Mock
	private EntityManager entityManager;

	private MobilityService velibService;
	private MobilityService ratpService;

	@BeforeEach
	void setUp() {
		velibService = new MobilityService("Vélib", "Service de vélos en libre-service");
		velibService.setServiceId(1);

		ratpService = new MobilityService("RATP", "Régie autonome des transports parisiens");
		ratpService.setServiceId(2);
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
		assertThat(result.getServiceId()).isEqualTo(1);
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
		when(mobilityServiceRepository.findById(1)).thenReturn(Optional.of(velibService));

		// When
		Optional<MobilityServiceDTO> result = mobilityServiceService.getMobilityServiceById(1);

		// Then
		assertThat(result).isPresent();
		assertThat(result.get().getName()).isEqualTo("Vélib");
		verify(mobilityServiceRepository).findById(1);
	}

	@Test
	void whenGetNonExistingMobilityServiceById_thenReturnEmpty() {
		// Given
		when(mobilityServiceRepository.findById(99)).thenReturn(Optional.empty());

		// When
		Optional<MobilityServiceDTO> result = mobilityServiceService.getMobilityServiceById(99);

		// Then
		assertThat(result).isEmpty();
		verify(mobilityServiceRepository).findById(99);
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
		when(mobilityServiceRepository.existsById(1)).thenReturn(true); // Changé de findById à existsById
		doNothing().when(mobilityServiceRepository).deleteById(1);

		// When
		boolean result = mobilityServiceService.deleteMobilityService(1);

		// Then
		assertThat(result).isTrue();
		verify(mobilityServiceRepository, times(1)).existsById(1); // Changé ici aussi
		verify(mobilityServiceRepository, times(1)).deleteById(1);
	}

	@Test
	void whenDeleteNonExistingMobilityService_thenReturnFalse() {
		// Given
		when(mobilityServiceRepository.existsById(99)).thenReturn(false); // Changé de findById à existsById

		// When
		boolean result = mobilityServiceService.deleteMobilityService(99);

		// Then
		assertThat(result).isFalse();
		verify(mobilityServiceRepository, times(1)).existsById(99); // Changé ici aussi
		verify(mobilityServiceRepository, never()).deleteById(any());
	}

}