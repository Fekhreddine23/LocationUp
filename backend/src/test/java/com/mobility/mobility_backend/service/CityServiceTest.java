package com.mobility.mobility_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.mobility.mobility_backend.dto.CityDTO;
import com.mobility.mobility_backend.entity.City;
import com.mobility.mobility_backend.repository.CityRepository;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("ci")
public class CityServiceTest {

	@Mock
	private CityRepository cityRepository;

	@InjectMocks
	private CityServiceImpl cityService;

	@Test
	public void testCreateCity() {
		// Given
		String cityName = "Paris";
		City savedCity = new City(cityName);
		savedCity.setId(1);

		when(cityRepository.existsByName(cityName)).thenReturn(false);
		when(cityRepository.save(any(City.class))).thenReturn(savedCity);

		// When
		CityDTO result = cityService.createCity(cityName);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getName()).isEqualTo(cityName);

		verify(cityRepository).existsByName(cityName);
		verify(cityRepository).save(any(City.class));
	}

	@Test
	public void testGetCityById() {
		// Given
		Integer cityId = 1;
		City city = new City("Paris");
		city.setId(cityId);

		when(cityRepository.findById(cityId)).thenReturn(Optional.of(city));

		// When
		Optional<CityDTO> result = cityService.getCityById(cityId);

		// Then
		assertThat(result).isPresent();
		assertThat(result.get().getId()).isEqualTo(cityId);
		assertThat(result.get().getName()).isEqualTo("Paris");
	}

}
