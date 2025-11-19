package com.mobility.mobility_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cities")
public class City {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "city_id") // ✅ Ajouter cette annotation
	private Integer cityId; // ✅ CHANGER Long → Integer

	@Column(nullable = false, unique = false)
	private String name;

	@Column(nullable = true, unique = false)
	private String postalCode;

	@Column(name = "latitude", nullable = true)
	private Double latitude;

	@Column(name = "longitude", nullable = true)
	private Double longitude;

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	// Constructeurs
	public City() {
	}

	public City(String name) {
		this.name = name;
	}

	public City(String name, String postalCode, Double latitude, Double longitude) {
		this.name = name;
		this.postalCode = postalCode;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	// Getters et Setters
	public Integer getCityId() {
		return cityId;
	}

	public void setId(Integer id) {
		this.cityId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof City)) {
			return false;
		}
		City city = (City) o;
		return cityId != null && cityId.equals(city.cityId);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return "City [cityId=" + cityId + ", name=" + name + ", postalCode=" + postalCode + ", latitude=" + latitude
				+ ", longitude=" + longitude + "]";
	}
}
