package com.mobility.mobility_backend.dto;

import java.time.LocalDateTime;

public class ReservationDTO {
	private Integer reservationId;
	private Integer userId;
	private Integer offerId;
	private LocalDateTime reservationDate;
	private String status;
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

		public ReservationDTO build() {
			return new ReservationDTO(reservationId, userId, offerId, reservationDate, status);
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