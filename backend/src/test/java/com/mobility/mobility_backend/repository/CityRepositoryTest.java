package com.mobility.mobility_backend.repository;

import com.mobility.mobility_backend.entity.City;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("ci")  // ✅ AJOUTÉ
public class CityRepositoryTest {

    @Autowired
    private CityRepository cityRepository;

    @Test
    public void testSaveAndFindCity() {
        // Given
        City city = new City("Paris");
        
        // When
        City savedCity = cityRepository.save(city);
        Optional<City> foundCity = cityRepository.findById(savedCity.getId());
        
        // Then
        assertThat(foundCity).isPresent();
        assertThat(foundCity.get().getName()).isEqualTo("Paris");
    }

    @Test
    public void testFindByName() {
        // Given
        City city = new City("Lyon");
        cityRepository.save(city);
        
        // When
        Optional<City> foundCity = cityRepository.findByName("Lyon");
        
        // Then
        assertThat(foundCity).isPresent();
        assertThat(foundCity.get().getName()).isEqualTo("Lyon");
    }

    @Test
    public void testExistsByName() {
        // Given
        City city = new City("Marseille");
        cityRepository.save(city);
        
        // When
        boolean exists = cityRepository.existsByName("Marseille");
        boolean notExists = cityRepository.existsByName("Toulouse");
        
        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}