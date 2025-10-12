package com.mobility.mobility_backend.repository;

import com.mobility.mobility_backend.entity.City;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CityRepositoryTest {
    
    @Test
    public void testCityEntity() {
        // Test simple de l'entité sans Spring
        City city = new City("Paris");
        
        assertThat(city).isNotNull();
        assertThat(city.getName()).isEqualTo("Paris");
        assertThat(city.toString()).contains("Paris");
        
        System.out.println("✅ TEST RÉUSSI : Entité City fonctionne !");
    }
    
    @Test
    public void testCitySettersGetters() {
        City city = new City();
        city.setId(1L);
        city.setName("Lyon");
        
        assertThat(city.getId()).isEqualTo(1L);
        assertThat(city.getName()).isEqualTo("Lyon");
        
        System.out.println("✅ TEST RÉUSSI : Getters/Setters fonctionnent !");
    }
}