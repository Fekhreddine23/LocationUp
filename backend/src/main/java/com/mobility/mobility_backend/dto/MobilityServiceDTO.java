package com.mobility.mobility_backend.dto;

public class MobilityServiceDTO {
	private Integer serviceId;
	private String name;
	private String description;

	// Constructeurs
	public MobilityServiceDTO() {
	}

	public MobilityServiceDTO(Integer integer, String name, String description) {
		this.serviceId = integer;
		this.name = name;
		this.description = description;
	}

	// Getters et Setters
	public Integer getServiceId() {
		return serviceId;
	}

	public void setServiceId(Integer serviceId) {
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