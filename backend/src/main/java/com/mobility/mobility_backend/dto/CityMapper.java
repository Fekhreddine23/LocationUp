package com.mobility.mobility_backend.dto;

import org.springframework.stereotype.Component;

import com.mobility.mobility_backend.entity.City; // ✅ AJOUT du ;

@Component
public class CityMapper {

	public static CityDTO toDTO(City city) {
		if (city == null) {
			return null;
		}
		return new CityDTO(city.getCityId(), city.getName());
	}

	public static City toEntity(CityDTO cityDTO) {
		if (cityDTO == null) {
			return null;
		}
		City city = new City(cityDTO.getName());
		city.setId(cityDTO.getId()); // ✅ CORRIGER setId → setCityId
		return city;
	}
}