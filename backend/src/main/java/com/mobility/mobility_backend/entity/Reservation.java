package com.mobility.mobility_backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "reservations")
public class Reservation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reservation_id")
	private Integer reservationId;

	@ManyToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "offer_id", referencedColumnName = "offer_id", nullable = false)
	private Offer offer;

	@NotNull(message = "La date de reservation ne peut pas être nulle")
	@Column(name = "reservation_date", nullable = false)
	private LocalDateTime reservationDate;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private ReservationStatus status;

	@Column(name = "payment_status")
	@Enumerated(EnumType.STRING)
	private PaymentStatus paymentStatus = PaymentStatus.PENDING;

	@Column(name = "payment_reference", length = 120)
	private String paymentReference;

	@Column(name = "payment_date")
	private LocalDateTime paymentDate;

	@Column(name = "payment_amount", precision = 10, scale = 2)
	private BigDecimal paymentAmount;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "driver_license_number", length = 120)
	private String driverLicenseNumber;

	@Column(name = "driver_license_country", length = 8)
	private String driverLicenseCountry;

	@Column(name = "driver_license_category", length = 30)
	private String driverLicenseCategory;

	@Column(name = "driver_license_expiry")
	private LocalDate driverLicenseExpiry;

	@Column(name = "driver_usage_reason", length = 512)
	private String driverUsageReason;

	@Column(name = "driver_km_per_year")
	private Integer driverKmPerYear;

	@Column(name = "driver_notes", length = 512)
	private String driverNotes;

	@Column(name = "driver_profile_completed_at")
	private LocalDateTime driverProfileCompletedAt;

	// Getters and Setters

	public Integer getReservationId() {
		return reservationId;
	}

	public void setReservationId(Integer reservationId) {
		this.reservationId = reservationId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Offer getOffer() {
		return offer;
	}

	public void setOffer(Offer offer) {
		this.offer = offer;
	}

	public @NotNull(message = "La date de reservation ne peut pas être nulle") LocalDateTime getReservationDate() {
		return reservationDate;
	}

	public void setReservationDate(@NotNull(message = "La date de reservation ne peut pas être nulle") LocalDateTime reservationDate) {
		this.reservationDate = reservationDate;
	}

	public ReservationStatus getStatus() {
		return status;
	}

	public void setStatus(ReservationStatus status) {
		this.status = status;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getPaymentReference() {
		return paymentReference;
	}

	public void setPaymentReference(String paymentReference) {
		this.paymentReference = paymentReference;
	}

	public LocalDateTime getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(LocalDateTime paymentDate) {
		this.paymentDate = paymentDate;
	}

	public BigDecimal getPaymentAmount() {
		return paymentAmount;
	}

	public void setPaymentAmount(BigDecimal paymentAmount) {
		this.paymentAmount = paymentAmount;
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

	public String getDriverLicenseNumber() {
		return driverLicenseNumber;
	}

	public void setDriverLicenseNumber(String driverLicenseNumber) {
		this.driverLicenseNumber = driverLicenseNumber;
	}

	public String getDriverLicenseCountry() {
		return driverLicenseCountry;
	}

	public void setDriverLicenseCountry(String driverLicenseCountry) {
		this.driverLicenseCountry = driverLicenseCountry;
	}

	public String getDriverLicenseCategory() {
		return driverLicenseCategory;
	}

	public void setDriverLicenseCategory(String driverLicenseCategory) {
		this.driverLicenseCategory = driverLicenseCategory;
	}

	public LocalDate getDriverLicenseExpiry() {
		return driverLicenseExpiry;
	}

	public void setDriverLicenseExpiry(LocalDate driverLicenseExpiry) {
		this.driverLicenseExpiry = driverLicenseExpiry;
	}

	public String getDriverUsageReason() {
		return driverUsageReason;
	}

	public void setDriverUsageReason(String driverUsageReason) {
		this.driverUsageReason = driverUsageReason;
	}

	public Integer getDriverKmPerYear() {
		return driverKmPerYear;
	}

	public void setDriverKmPerYear(Integer driverKmPerYear) {
		this.driverKmPerYear = driverKmPerYear;
	}

	public String getDriverNotes() {
		return driverNotes;
	}

	public void setDriverNotes(String driverNotes) {
		this.driverNotes = driverNotes;
	}

	public LocalDateTime getDriverProfileCompletedAt() {
		return driverProfileCompletedAt;
	}

	public void setDriverProfileCompletedAt(LocalDateTime driverProfileCompletedAt) {
		this.driverProfileCompletedAt = driverProfileCompletedAt;
	}

	public enum ReservationStatus {
		PENDING, CONFIRMED, CANCELLED, COMPLETED
	}

	public enum PaymentStatus {
		PENDING, REQUIRES_ACTION, PAID, FAILED, REFUNDED, EXPIRED
	}

}
