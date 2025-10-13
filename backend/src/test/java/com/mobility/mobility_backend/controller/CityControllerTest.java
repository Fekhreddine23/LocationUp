package com.mobility.mobility_backend.controller;

import com.mobility.mobility_backend.dto.CityDTO;
import com.mobility.mobility_backend.service.CityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CityController.class)
@ActiveProfiles("ci")
public class CityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CityService cityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateCity() throws Exception {
        // Given
        CityDTO requestDTO = new CityDTO(null, "Paris");
        CityDTO responseDTO = new CityDTO(1L, "Paris");
        
        when(cityService.createCity(any(String.class))).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/cities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Paris"));
    }

    @Test
    public void testGetAllCities() throws Exception {
        // Given
        CityDTO city1 = new CityDTO(1L, "Paris");
        CityDTO city2 = new CityDTO(2L, "Lyon");
        List<CityDTO> cities = Arrays.asList(city1, city2);
        
        when(cityService.getAllCities()).thenReturn(cities);

        // When & Then
        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Paris"))
                .andExpect(jsonPath("$[1].name").value("Lyon"));
    }

    @Test
    public void testGetCityById() throws Exception {
        // Given
        CityDTO city = new CityDTO(1L, "Paris");
        
        when(cityService.getCityById(1L)).thenReturn(Optional.of(city));

        // When & Then
        mockMvc.perform(get("/api/cities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Paris"));
    }

    @Test
    public void testGetCityByIdNotFound() throws Exception {
        // Given
        when(cityService.getCityById(99L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/cities/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetCityByName() throws Exception {
        // Given
        CityDTO city = new CityDTO(1L, "Paris");
        
        when(cityService.getCityByName("Paris")).thenReturn(Optional.of(city));

        // When & Then
        mockMvc.perform(get("/api/cities/name/Paris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Paris"));
    }
}
