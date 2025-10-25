package com.mobility.mobility_backend.dto;

import org.springframework.stereotype.Component;

import com.mobility.mobility_backend.entity.Reservation;

@Component
public class ReservationMapper {

	public ReservationDTO toDTO(Reservation reservation) {
		if (reservation == null) {
			return null;
		}

		return ReservationDTO.builder().reservationId(reservation.getReservationId())
				.userId(reservation.getUser() != null ? reservation.getUser().getId() : null)
				.offerId(reservation.getOffer() != null ? reservation.getOffer().getOfferId() : null)
				.reservationDate(reservation.getReservationDate())
				.status(reservation.getStatus() != null ? reservation.getStatus().name() : null).build();
	}

	public Reservation toEntity(ReservationDTO reservationDTO) {
		if (reservationDTO == null) {
			return null;
		}

		Reservation reservation = new Reservation();
		reservation.setReservationId(reservationDTO.getReservationId());
		reservation.setReservationDate(reservationDTO.getReservationDate());

		if (reservationDTO.getStatus() != null) {
			try {
				reservation.setStatus(Reservation.ReservationStatus.valueOf(reservationDTO.getStatus()));
			} catch (IllegalArgumentException e) {
				reservation.setStatus(Reservation.ReservationStatus.PENDING);
			}
		}

		return reservation;
	}

	public void updateEntityFromDTO(ReservationDTO reservationDTO, Reservation reservation) {
		if (reservationDTO == null || reservation == null) {
			return;
		}

		if (reservationDTO.getReservationDate() != null) {
			reservation.setReservationDate(reservationDTO.getReservationDate());
		}
		if (reservationDTO.getStatus() != null) {
			try {
				reservation.setStatus(Reservation.ReservationStatus.valueOf(reservationDTO.getStatus()));
			} catch (IllegalArgumentException e) {
				// Garder le statut actuel en cas d'erreur
			}
		}
	}
}