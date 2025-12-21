package com.mobility.mobility_backend.dto.driver;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DriverProfileDTO {

	private Long id;
	private Integer userId;
	private String licenseNumber;
	private String licenseCountry;
	private String licenseCategory;
	private LocalDate licenseIssuedOn;
	private LocalDate licenseExpiresOn;
	private Integer annualKilometers;
	private String usageReason;
	private String notes;
	private LocalDateTime updatedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getLicenseNumber() {
		return licenseNumber;
	}

	public void setLicenseNumber(String licenseNumber) {
		this.licenseNumber = licenseNumber;
	}

	public String getLicenseCountry() {
		return licenseCountry;
	}

	public void setLicenseCountry(String licenseCountry) {
		this.licenseCountry = licenseCountry;
	}

	public String getLicenseCategory() {
		return licenseCategory;
	}

	public void setLicenseCategory(String licenseCategory) {
		this.licenseCategory = licenseCategory;
	}

	public LocalDate getLicenseIssuedOn() {
		return licenseIssuedOn;
	}

	public void setLicenseIssuedOn(LocalDate licenseIssuedOn) {
		this.licenseIssuedOn = licenseIssuedOn;
	}

	public LocalDate getLicenseExpiresOn() {
		return licenseExpiresOn;
	}

	public void setLicenseExpiresOn(LocalDate licenseExpiresOn) {
		this.licenseExpiresOn = licenseExpiresOn;
	}

	public Integer getAnnualKilometers() {
		return annualKilometers;
	}

	public void setAnnualKilometers(Integer annualKilometers) {
		this.annualKilometers = annualKilometers;
	}

	public String getUsageReason() {
		return usageReason;
	}

	public void setUsageReason(String usageReason) {
		this.usageReason = usageReason;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
