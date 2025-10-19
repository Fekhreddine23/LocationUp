package com.mobility.mobility_backend.service;

import java.util.List;
import java.util.Optional;

import com.mobility.mobility_backend.dto.CityDTO;

public interface CityService {
	CityDTO createCity(String name);

	Optional<CityDTO> getCityById(Integer cityId);

	List<CityDTO> getAllCities();

	Optional<CityDTO> getCityByName(String name);

	boolean cityExists(String name);

	boolean deleteCity(Integer id); // ✅ CHANGER Long → Integer
}