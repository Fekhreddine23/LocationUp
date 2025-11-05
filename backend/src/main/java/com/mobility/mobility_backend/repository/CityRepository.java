package com.mobility.mobility_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobility.mobility_backend.entity.City;

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {
	Optional<City> findByName(String name);

	boolean existsByName(String name);

	 // Ajoutez cette méthode
    Optional<City> findByNameAndPostalCode(String name, String postalCode);
}
