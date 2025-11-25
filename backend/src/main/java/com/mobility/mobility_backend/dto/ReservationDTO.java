package com.mobility.mobility_backend.dto;

import java.time.LocalDateTime;

public class ReservationDTO {
	private Integer reservationId;
	private Integer userId;
	private Integer offerId;
private LocalDateTime reservationDate;
	private String status;
	private String paymentStatus;
	private String paymentReference;
	private LocalDateTime paymentDate;
	private Double paymentAmount;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	// Constructeurs
	public ReservationDTO() {
	}

	public ReservationDTO(Integer reservationId, Integer userId, Integer offerId, LocalDateTime reservationDate,
			String status) {
		this.reservationId = reservationId;
		this.userId = userId;
		this.offerId = offerId;
		this.reservationDate = reservationDate;
		this.status = status;
	}

	// Builder
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Integer reservationId;
		private Integer userId;
		private Integer offerId;
		private LocalDateTime reservationDate;
		private String status;
		private String paymentStatus;
		private String paymentReference;
		private LocalDateTime paymentDate;
		private Double paymentAmount;

		public Builder reservationId(Integer reservationId) {
			this.reservationId = reservationId;
			return this;
		}

		public Builder userId(Integer userId) {
			this.userId = userId;
			return this;
		}

		public Builder offerId(Integer offerId) {
			this.offerId = offerId;
			return this;
		}

		public Builder reservationDate(LocalDateTime reservationDate) {
			this.reservationDate = reservationDate;
			return this;
		}

		public Builder status(String status) {
			this.status = status;
			return this;
		}

		public Builder paymentStatus(String paymentStatus) {
			this.paymentStatus = paymentStatus;
			return this;
		}

		public Builder paymentReference(String paymentReference) {
			this.paymentReference = paymentReference;
			return this;
		}

		public Builder paymentDate(LocalDateTime paymentDate) {
			this.paymentDate = paymentDate;
			return this;
		}

		public Builder paymentAmount(Double paymentAmount) {
			this.paymentAmount = paymentAmount;
			return this;
		}

		public ReservationDTO build() {
			ReservationDTO dto = new ReservationDTO(reservationId, userId, offerId, reservationDate, status);
			dto.setPaymentStatus(paymentStatus);
			dto.setPaymentReference(paymentReference);
			dto.setPaymentDate(paymentDate);
			dto.setPaymentAmount(paymentAmount);
			return dto;
		}
	}

	// Getters et Setters
	public Integer getReservationId() {
		return reservationId;
	}

	public void setReservationId(Integer reservationId) {
		this.reservationId = reservationId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getOfferId() {
		return offerId;
	}

	public void setOfferId(Integer offerId) {
		this.offerId = offerId;
	}

	public LocalDateTime getReservationDate() {
		return reservationDate;
	}

	public void setReservationDate(LocalDateTime reservationDate) {
		this.reservationDate = reservationDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
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

	public Double getPaymentAmount() {
		return paymentAmount;
	}

	public void setPaymentAmount(Double paymentAmount) {
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
}
