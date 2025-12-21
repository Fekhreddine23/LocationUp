package com.mobility.mobility_backend.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "driver_profiles")
public class DriverProfile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", unique = true)
	private User user;

	@Column(name = "license_number", length = 120)
	private String licenseNumber;

	@Column(name = "license_country", length = 8)
	private String licenseCountry;

	@Column(name = "license_category", length = 30)
	private String licenseCategory;

	@Column(name = "license_issued_on")
	private LocalDate licenseIssuedOn;

	@Column(name = "license_expires_on")
	private LocalDate licenseExpiresOn;

	@Column(name = "annual_kilometers")
	private Integer annualKilometers;

	@Column(name = "usage_reason", length = 512)
	private String usageReason;

	@Column(name = "notes", length = 512)
	private String notes;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	public void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	public void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
