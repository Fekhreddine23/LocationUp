package com.mobility.mobility_backend.repository;

import com.mobility.mobility_backend.entity.City;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("ci")
@Transactional  // ✅ Utiliser @Transactional
public class CityRepositoryTest {

    @Autowired
    private CityRepository cityRepository;

    @Test
    public void testSaveAndFindCity() {
        // Given
        String uniqueName = "TestCity_" + System.currentTimeMillis();
        City city = new City(uniqueName);
        
        // When
        City savedCity = cityRepository.save(city);
        Optional<City> foundCity = cityRepository.findById(savedCity.getId());
        
        // Then
        assertThat(foundCity).isPresent();
        assertThat(foundCity.get().getName()).isEqualTo(uniqueName);
    }

    @Test
    public void testFindByName() {
        // Given
        String uniqueName = "TestFind_" + System.currentTimeMillis();
        City city = new City(uniqueName);
        cityRepository.save(city);
        
        // When
        Optional<City> foundCity = cityRepository.findByName(uniqueName);
        
        // Then
        assertThat(foundCity).isPresent();
        assertThat(foundCity.get().getName()).isEqualTo(uniqueName);
    }

    @Test
    public void testExistsByName() {
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
}
