package com.mobility.mobility_backend.dto;

import org.springframework.stereotype.Component;

import com.mobility.mobility_backend.entity.MobilityService;

@Component
public class MobilityServiceMapper {

	public static MobilityServiceDTO toDTO(MobilityService mobilityService) {
		if (mobilityService == null) {
			return null;
		}
		return new MobilityServiceDTO(mobilityService.getServiceId(), mobilityService.getName(),
				mobilityService.getDescription());
	}

	public static MobilityService toEntity(MobilityServiceDTO mobilityServiceDTO) {
		if (mobilityServiceDTO == null) {
			return null;
		}
		MobilityService mobilityService = new MobilityService(mobilityServiceDTO.getName(),
				mobilityServiceDTO.getDescription());
		mobilityService.setServiceId(mobilityServiceDTO.getServiceId());
		return mobilityService;
	}
}