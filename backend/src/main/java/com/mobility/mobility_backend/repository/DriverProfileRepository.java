package com.mobility.mobility_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobility.mobility_backend.entity.DriverProfile;

public interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {

	Optional<DriverProfile> findByUser_Id(Integer userId);
}
