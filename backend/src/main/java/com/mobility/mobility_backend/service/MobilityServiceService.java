package com.mobility.mobility_backend.service;

import java.util.List;
import java.util.Optional;

import com.mobility.mobility_backend.dto.MobilityServiceDTO;

public interface MobilityServiceService {

	MobilityServiceDTO createMobilityService(String name, String description);

	Optional<MobilityServiceDTO> getMobilityServiceByName(String name);

	List<MobilityServiceDTO> getAllMobilityServices();

	boolean mobilityServiceExists(String name);

	boolean deleteMobilityService(Integer id);

	Optional<MobilityServiceDTO> getMobilityServiceById(Integer id);

}