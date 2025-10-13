package com.mobility.mobility_backend.repository;

import com.mobility.mobility_backend.entity.MobilityService;
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
class MobilityServiceRepositoryTest {

    @Autowired
    private MobilityServiceRepository mobilityServiceRepository;

    @Test
    void testSimpleSave() {
        String uniqueName = "TestService_" + System.currentTimeMillis();
        MobilityService service = new MobilityService(uniqueName, "Test Description");
        MobilityService saved = mobilityServiceRepository.save(service);
        
        assertThat(saved.getServiceId()).isNotNull();
        assertThat(saved.getName()).isEqualTo(uniqueName);
    }

    @Test
    void testFindByName() {
        String uniqueName = "TestFind_" + System.currentTimeMillis();
        MobilityService service = new MobilityService(uniqueName, "Service de test");
        mobilityServiceRepository.save(service);
        
        Optional<MobilityService> found = mobilityServiceRepository.findByName(uniqueName);
        
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo(uniqueName);
    }

    @Test
    void testExistsByName() {
        String uniqueName = "TestExists_" + System.currentTimeMillis();
        MobilityService service = new MobilityService(uniqueName, "Service de test");
        mobilityServiceRepository.save(service);
        
        boolean exists = mobilityServiceRepository.existsByName(uniqueName);
        boolean notExists = mobilityServiceRepository.existsByName("NonExistent_" + System.currentTimeMillis());
        
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
