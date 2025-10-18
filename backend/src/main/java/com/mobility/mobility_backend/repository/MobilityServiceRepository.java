package com.mobility.mobility_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobility.mobility_backend.entity.MobilityService;

@Repository
public interface MobilityServiceRepository extends JpaRepository<MobilityService, Integer> {

    Optional<MobilityService> findByName(String name);

    boolean existsByName(String name);
}