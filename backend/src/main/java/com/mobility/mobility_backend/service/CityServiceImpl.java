package com.mobility.mobility_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mobility.mobility_backend.dto.CityDTO;
import com.mobility.mobility_backend.dto.CityMapper;
import com.mobility.mobility_backend.entity.City;
import com.mobility.mobility_backend.repository.CityRepository;

@Service
public class CityServiceImpl implements CityService {

	@Autowired
	private CityRepository cityRepository;

	@Override
	public CityDTO createCity(String name) {
		// Vérifier si la ville existe déjà
		if (cityRepository.existsByName(name)) {
			throw new RuntimeException("Une ville avec le nom '" + name + "' existe déjà");
		}

		// Créer et sauvegarder la nouvelle ville
		City city = new City(name);
		City savedCity = cityRepository.save(city);

		// Convertir en DTO et retourner
		return CityMapper.toDTO(savedCity);
	}

	@Override
	public Optional<CityDTO> getCityById(Long id) {
		return cityRepository.findById(id).map(CityMapper::toDTO);
	}

	@Override
	public List<CityDTO> getAllCities() {
		return cityRepository.findAll().stream().map(CityMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public Optional<CityDTO> getCityByName(String name) {
		return cityRepository.findByName(name).map(CityMapper::toDTO);
	}

	@Override
	public boolean cityExists(String name) {
		return cityRepository.existsByName(name);
	}

	@Override
	public boolean deleteCity(Long id) {
		if (cityRepository.existsById(id)) {
			cityRepository.deleteById(id);
			return true;
		}
		return false;
	}

}
