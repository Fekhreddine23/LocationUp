package com.mobility.mobility_backend.entity;

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
import jakarta.validation.constraints.Future;
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
	@Future(message = "La date de reservation doit être dans le futur")
	@Column(name = "reservation_date", nullable = false)
	private LocalDateTime reservationDate;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private ReservationStatus status;

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

	public @NotNull(message = "La date de reservation ne peut pas être nulle") @Future(message = "La date de reservation doit être dans le futur") LocalDateTime getReservationDate() {
		return reservationDate;
	}

	public void setReservationDate(
			@NotNull(message = "La date de reservation ne peut pas être nulle") @Future(message = "La date de reservation doit être dans le futur") LocalDateTime reservationDate) {
		this.reservationDate = reservationDate;
	}

	public ReservationStatus getStatus() {
		return status;
	}

	public void setStatus(ReservationStatus status) {
		this.status = status;
	}

	public enum ReservationStatus {
		PENDING, CONFIRMED, CANCELLED, COMPLETED
	}


}