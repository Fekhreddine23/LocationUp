package com.mobility.mobility_backend.service;


import com.mobility.mobility_backend.dto.MobilityServiceDTO;
import java.util.List;
import java.util.Optional;

public interface MobilityServiceService {
    
    MobilityServiceDTO createMobilityService(String name, String description);
    
    Optional<MobilityServiceDTO> getMobilityServiceById(Long id);
    
    Optional<MobilityServiceDTO> getMobilityServiceByName(String name);
    
    List<MobilityServiceDTO> getAllMobilityServices();
    
    boolean mobilityServiceExists(String name);
    
    boolean deleteMobilityService(Long id);
}