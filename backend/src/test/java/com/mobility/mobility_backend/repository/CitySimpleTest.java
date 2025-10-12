package com.mobility.mobility_backend.repository;

import com.mobility.mobility_backend.entity.City;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CitySimpleTest {
    
    @Test
    public void testCityCreation() {
        // Test simple sans base de données
        City city = new City("Paris");
        
        assertThat(city.getName()).isEqualTo("Paris");
        assertThat(city).isNotNull();
        
        System.out.println("✅ TEST RÉUSSI : City créée avec nom = " + city.getName());
    }
    
    @Test
    public void testCityGettersSetters() {
        City city = new City();
        city.setId(1L);
        city.setName("Lyon");
        
        assertThat(city.getId()).isEqualTo(1L);
        assertThat(city.getName()).isEqualTo("Lyon");
        
        System.out.println("✅ TEST RÉUSSI : Getters/Setters fonctionnent");
    }
}
