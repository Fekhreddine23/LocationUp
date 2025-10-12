package com.mobility.mobility_backend.controller;

import com.mobility.mobility_backend.config.TestSecurityConfig;
import com.mobility.mobility_backend.dto.CityDTO;
import com.mobility.mobility_backend.service.CityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ✅ Configuration pour désactiver la sécurité
@Import(TestSecurityConfig.class)
@WebMvcTest(CityController.class)
public class CityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CityService cityService;

    @Test
    public void testCreateCity() throws Exception {
        // Given
        CityDTO cityDTO = new CityDTO(1L, "Paris");
        when(cityService.createCity(any(String.class))).thenReturn(cityDTO);

        // When & Then
        mockMvc.perform(post("/api/cities")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Paris\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Paris"));
    }

    @Test
    public void testGetAllCities() throws Exception {
        // Given
        CityDTO city1 = new CityDTO(1L, "Paris");
        CityDTO city2 = new CityDTO(2L, "Lyon");
        when(cityService.getAllCities()).thenReturn(Arrays.asList(city1, city2));

        // When & Then
        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Paris"))
                .andExpect(jsonPath("$[1].name").value("Lyon"));
    }
}