package com.mobility.mobility_backend.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = NoWebTest.TestConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = { "spring.main.web-application-type=none",
		"management.endpoints.enabled-by-default=false" })
public class NoWebTest {

	@Test
	void testWithMinimalContext() {
		System.out.println("✅ Contexte Spring minimal chargé !");
		assertTrue(true);
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class,
			org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class })
	static class TestConfig {
	}
}