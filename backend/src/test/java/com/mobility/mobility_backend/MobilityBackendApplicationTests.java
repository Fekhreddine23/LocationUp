package com.mobility.mobility_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("ci")  // ✅ UTILISE LE PROFIL CI
class MobilityBackendApplicationTests {

    @Test
    void contextLoads() {
        // Test simple de chargement du contexte
    }
}
