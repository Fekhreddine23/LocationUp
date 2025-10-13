package com.mobility.mobility_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "mobility_services")
public class MobilityService {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "service_id")
	private Long serviceId;

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 500)
	private String description;

	// Constructeurs
	public MobilityService() {
	}

	public MobilityService(String name, String description) {
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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof MobilityService))
			return false;
		MobilityService that = (MobilityService) o;
		return serviceId != null && serviceId.equals(that.serviceId);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return "MobilityService{" + "serviceId=" + serviceId + ", name='" + name + '\'' + ", description='"
				+ description + '\'' + '}';
	}
}