package com.mobility.mobility_backend.dto;

import java.time.LocalDateTime;

import com.mobility.mobility_backend.dto.driver.DriverProfileDTO;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public class ReservationCreationDTO {

	@NotNull(message = "L'utilisateur est obligatoire")
	private Integer userId;

	@NotNull(message = "L'offre est obligatoire")
	private Integer offerId;

	@NotNull(message = "La date de réservation est obligatoire")
	@Future(message = "La date de reservation doit être dans le futur")
	private LocalDateTime reservationDate;

	private DriverProfileDTO driverProfile;

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

	public DriverProfileDTO getDriverProfile() {
		return driverProfile;
	}

	public void setDriverProfile(DriverProfileDTO driverProfile) {
		this.driverProfile = driverProfile;
	}
}
