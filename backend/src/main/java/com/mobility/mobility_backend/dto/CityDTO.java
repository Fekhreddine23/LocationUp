package com.mobility.mobility_backend.dto;

public class CityDTO {
	private Integer id;
	private String name;
	private String postalCode;
	private Double latitude;
	private Double longitude;

	// Constructeurs
	public CityDTO() {
	}

	public CityDTO(Integer id, String name, String postalCode, Double latitude, Double longitude) {
		this.id = id;
		this.name = name;
		this.postalCode = postalCode;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	// Getters et Setters
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return "CityDTO{id=" + id + ", name='" + name + "', postalCode='" + postalCode + "', latitude=" + latitude
				+ ", longitude=" + longitude + "}";
	}
}
