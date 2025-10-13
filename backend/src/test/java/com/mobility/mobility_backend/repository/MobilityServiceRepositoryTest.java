package com.mobility.mobility_backend.repository;

import com.mobility.mobility_backend.entity.MobilityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("ci")
class MobilityServiceRepositoryTest {

    @Autowired
    private MobilityServiceRepository mobilityServiceRepository;

    @Test
    void testSimpleSave() {
        // Test le plus basique possible avec un nom unique
        MobilityService service = new MobilityService("TestService_Unique_1", "Test Description");
        MobilityService saved = mobilityServiceRepository.save(service);
        
        assertThat(saved.getServiceId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("TestService_Unique_1");
    }

    @Test
    void testFindByName() {
        // Given - utiliser un nom qui n'existe pas dans schema-test.sql
        MobilityService service = new MobilityService("TestFindService", "Service de test pour find");
        mobilityServiceRepository.save(service);
        
        // When
        Optional<MobilityService> found = mobilityServiceRepository.findByName("TestFindService");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("TestFindService");
    }

    @Test
    void testExistsByName() {
        // Given - utiliser un nom qui n'existe pas dans schema-test.sql
        MobilityService service = new MobilityService("TestExistsService", "Service de test pour exists");
        mobilityServiceRepository.save(service);
        
        // When
        boolean exists = mobilityServiceRepository.existsByName("TestExistsService");
        boolean notExists = mobilityServiceRepository.existsByName("NonExistentServiceXYZ");
        
        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void testFindByNameWithExistingData() {
        // Test avec les données existantes de schema-test.sql
        Optional<MobilityService> found = mobilityServiceRepository.findByName("Vélib");
        
        // Then - devrait trouver les données pré-chargées
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Vélib");
        assertThat(found.get().getDescription()).isEqualTo("Service de vélos en libre-service à Paris");
    }

    @Test
    void testExistsByNameWithExistingData() {
        // Test avec les données existantes de schema-test.sql
        boolean exists = mobilityServiceRepository.existsByName("Lime");
        boolean notExists = mobilityServiceRepository.existsByName("NonExistentService123");
        
        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}