package com.mobility.mobility_backend.dto;

public class MobilityServiceDTO {
	private Long serviceId;
	private String name;
	private String description;

	// Constructeurs
	public MobilityServiceDTO() {
	}

	public MobilityServiceDTO(Long serviceId, String name, String description) {
		this.serviceId = serviceId;
		this.name = name;
		this.description = description;
	}

	// Getters et Setters
	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}