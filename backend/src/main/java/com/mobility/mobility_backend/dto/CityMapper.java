package com.mobility.mobility_backend.dto;

import com.mobility.mobility_backend.entity.City;

public class CityMapper {
    
    public static CityDTO toDTO(City city) {
        if (city == null) {
            return null;
        }
        return new CityDTO(city.getId(), city.getName());
    }
    
    public static City toEntity(CityDTO cityDTO) {
        if (cityDTO == null) {
            return null;
        }
        City city = new City(cityDTO.getName());
        city.setId(cityDTO.getId());
        return city;
    }
}
