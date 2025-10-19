package com.mobility.mobility_backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.mobility.mobility_backend.entity.City;

@DataJpaTest
@ActiveProfiles("test")
class CitySimpleTest {

	@Autowired
	private CityRepository cityRepository;

	@Test
	void whenFindById_thenReturnCity() {
		// Given
		City city = cityRepository.save(new City("Paris"));

		// When
		Optional<City> foundCity = cityRepository.findById(city.getCityId()); // ✅ Utiliser getCityId()

		// Then
		assertThat(foundCity).isPresent();
		assertThat(foundCity.get().getName()).isEqualTo("Paris");
	}

	@Test
	void contextLoads() {
		assertThat(cityRepository).isNotNull();
	}
}