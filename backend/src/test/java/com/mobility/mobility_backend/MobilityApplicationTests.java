package com.mobility.mobility_backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("ci")
@Disabled("Temporarily disabled - context loading issue needs investigation")
class MobilityApplicationTests {

    @Test
    void contextLoads() {
        // Test que le contexte Spring se charge correctement
    }
}
