package com.mobility.mobility_backend.config;



import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // DÉSACTIVER COMPLÈTEMENT LA SÉCURITÉ
        http.csrf().disable()
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()  // ✅ TOUTES les requêtes sont autorisées
            );
        return http.build();
    }
}
