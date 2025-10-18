package com.mobility.mobility_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mobility.mobility_backend.dto.MobilityServiceDTO;
import com.mobility.mobility_backend.dto.MobilityServiceMapper;
import com.mobility.mobility_backend.entity.MobilityService;
import com.mobility.mobility_backend.repository.MobilityServiceRepository;

@Service
public class MobilityServiceServiceImpl implements MobilityServiceService {

    @Autowired
    private MobilityServiceRepository mobilityServiceRepository;

    @Override
    public MobilityServiceDTO createMobilityService(String name, String description) {
        // Vérifier si le service existe déjà
        if (mobilityServiceRepository.existsByName(name)) {
            throw new RuntimeException("Un service de mobilité avec le nom '" + name + "' existe déjà");
        }

        // Créer et sauvegarder le nouveau service
        MobilityService mobilityService = new MobilityService(name, description);
        MobilityService savedService = mobilityServiceRepository.save(mobilityService);

        // Convertir en DTO et retourner
        return MobilityServiceMapper.toDTO(savedService);
    }

    @Override
    public Optional<MobilityServiceDTO> getMobilityServiceById(Integer id) {
        return mobilityServiceRepository.findById(id)
                .map(MobilityServiceMapper::toDTO);
    }

    @Override
    public Optional<MobilityServiceDTO> getMobilityServiceByName(String name) {
        return mobilityServiceRepository.findByName(name).map(MobilityServiceMapper::toDTO);
    }

    @Override
    public List<MobilityServiceDTO> getAllMobilityServices() {
        return mobilityServiceRepository.findAll().stream().map(MobilityServiceMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean mobilityServiceExists(String name) {
        return mobilityServiceRepository.existsByName(name);
    }

    @Override
    public boolean deleteMobilityService(Integer id) {
        if (mobilityServiceRepository.existsById(id)) {
            mobilityServiceRepository.deleteById(id);
            return true;
        }
        return false;
    }
}