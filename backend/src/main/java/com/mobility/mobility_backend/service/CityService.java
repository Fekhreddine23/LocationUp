package com.mobility.mobility_backend.service;

import java.util.List;
import java.util.Optional;

import com.mobility.mobility_backend.dto.CityDTO;

public interface CityService {
	
	/**
     * Crée une nouvelle ville
     */
    CityDTO createCity(String name);
    
    /**
     * Récupère une ville par son ID
     */
    Optional<CityDTO> getCityById(Long id);
    
    /**
     * Récupère toutes les villes
     */
    List<CityDTO> getAllCities();
    
    /**
     * Récupère une ville par son nom
     */
    Optional<CityDTO> getCityByName(String name);
    
    /**
     * Vérifie si une ville existe par son nom
     */
    boolean cityExists(String name);
    
    /**
     * Supprime une ville par son ID
     */
    boolean deleteCity(Long id);
	

}
