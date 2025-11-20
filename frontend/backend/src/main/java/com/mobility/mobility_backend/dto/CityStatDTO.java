package com.mobility.mobility_backend.dto;

public class CityStatDTO {
	private String city;
	private long count;

	public CityStatDTO() {
	}

	public CityStatDTO(String city, long count) {
		this.city = city;
		this.count = count;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}
}
