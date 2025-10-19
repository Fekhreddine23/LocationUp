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
class CityRepositoryTest {

	@Autowired
	private CityRepository cityRepository;

	@Test
	void testSaveAndFindCity() {
		// Given
		String uniqueName = "TestCity_" + System.currentTimeMillis();
		City city = new City(uniqueName);

		// When
		City savedCity = cityRepository.save(city);
		Optional<City> foundCity = cityRepository.findById(savedCity.getCityId()); // ✅ Déjà Integer

		// Then
		assertThat(foundCity).isPresent();
		assertThat(foundCity.get().getName()).isEqualTo(uniqueName);
		assertThat(foundCity.get().getCityId()).isInstanceOf(Integer.class); // ✅ Vérifier que c'est Integer
	}

	@Test
	void testFindByName() {
		// Given
		String uniqueName = "TestFind_" + System.currentTimeMillis();
		City city = new City(uniqueName);
		cityRepository.save(city);

		// When
		Optional<City> foundCity = cityRepository.findByName(uniqueName);

		// Then
		assertThat(foundCity).isPresent();
		assertThat(foundCity.get().getName()).isEqualTo(uniqueName);
		assertThat(foundCity.get().getCityId()).isNotNull(); // ✅ Vérifier que l'ID est généré
	}

	@Test
	void testExistsByName() {
		// Given
		String uniqueName = "TestExists_" + System.currentTimeMillis();
		City city = new City(uniqueName);
		cityRepository.save(city);

		// When
		boolean exists = cityRepository.existsByName(uniqueName);
		boolean notExists = cityRepository.existsByName("NonExistent_" + System.currentTimeMillis());

		// Then
		assertThat(exists).isTrue();
		assertThat(notExists).isFalse();
	}

	@Test
	void testDeleteCity() {
		// Given
		String uniqueName = "TestDelete_" + System.currentTimeMillis();
		City city = new City(uniqueName);
		City savedCity = cityRepository.save(city);

		// When
		cityRepository.deleteById(savedCity.getCityId()); // ✅ Utiliser getCityId()

		// Then
		Optional<City> deletedCity = cityRepository.findById(savedCity.getCityId());
		assertThat(deletedCity).isEmpty();
	}

	@Test
	void testFindAllCities() {
		// Given
		cityRepository.save(new City("Paris"));
		cityRepository.save(new City("Lyon"));

		// When
		java.util.List<City> cities = cityRepository.findAll();

		// Then
		assertThat(cities).hasSize(2);
		assertThat(cities).extracting(City::getName).containsExactlyInAnyOrder("Paris", "Lyon");
	}
}
