package com.mobility.mobility_backend.repository;

import com.mobility.mobility_backend.entity.MobilityService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MobilityServiceRepository extends JpaRepository<MobilityService, Long> {
    
    Optional<MobilityService> findByName(String name);
    
    boolean existsByName(String name);
}