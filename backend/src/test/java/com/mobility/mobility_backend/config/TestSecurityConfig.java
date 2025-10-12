package com.mobility.mobility_backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // ✅ Active le profil test
public class TestSecurityConfig {

    @Test
    public void contextLoads() {
        // Test simple de chargement du contexte
    }
}
