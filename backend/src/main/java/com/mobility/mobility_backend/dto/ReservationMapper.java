package com.mobility.mobility_backend.dto;

import org.springframework.stereotype.Component;

import com.mobility.mobility_backend.entity.Reservation;

@Component
public class ReservationMapper {

	public ReservationDTO toDTO(Reservation reservation) {
		if (reservation == null) {
			return null;
		}

		ReservationDTO dto = ReservationDTO.builder().reservationId(reservation.getReservationId())
				.userId(reservation.getUser() != null ? reservation.getUser().getId() : null)
				.offerId(reservation.getOffer() != null ? reservation.getOffer().getOfferId() : null)
				.reservationDate(reservation.getReservationDate())
				.status(reservation.getStatus() != null ? reservation.getStatus().name() : null)
				.paymentStatus(reservation.getPaymentStatus() != null ? reservation.getPaymentStatus().name() : null)
				.paymentReference(reservation.getPaymentReference())
				.paymentDate(reservation.getPaymentDate())
				.paymentAmount(reservation.getPaymentAmount() != null ? reservation.getPaymentAmount().doubleValue() : null)
				.build();
		dto.setCreatedAt(reservation.getCreatedAt());
		dto.setUpdatedAt(reservation.getUpdatedAt());
		if (reservation.getUser() != null) {
			dto.setUserUsername(reservation.getUser().getUsername());
			dto.setUserEmail(reservation.getUser().getEmail());
		}
		return dto;
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

		if (reservationDTO.getPaymentStatus() != null) {
			try {
				reservation.setPaymentStatus(Reservation.PaymentStatus.valueOf(reservationDTO.getPaymentStatus()));
			} catch (IllegalArgumentException e) {
				reservation.setPaymentStatus(Reservation.PaymentStatus.PENDING);
			}
		}
		reservation.setPaymentReference(reservationDTO.getPaymentReference());
		if (reservationDTO.getPaymentDate() != null) {
			reservation.setPaymentDate(reservationDTO.getPaymentDate());
		}
		if (reservationDTO.getPaymentAmount() != null) {
			reservation.setPaymentAmount(new java.math.BigDecimal(reservationDTO.getPaymentAmount()));
		}
		if (reservationDTO.getCreatedAt() != null) {
			reservation.setCreatedAt(reservationDTO.getCreatedAt());
		}
		if (reservationDTO.getUpdatedAt() != null) {
			reservation.setUpdatedAt(reservationDTO.getUpdatedAt());
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

		if (reservationDTO.getPaymentStatus() != null) {
			try {
				reservation.setPaymentStatus(Reservation.PaymentStatus.valueOf(reservationDTO.getPaymentStatus()));
			} catch (IllegalArgumentException e) {
				// keep previous
			}
		}
		if (reservationDTO.getPaymentReference() != null) {
			reservation.setPaymentReference(reservationDTO.getPaymentReference());
		}
		if (reservationDTO.getPaymentDate() != null) {
			reservation.setPaymentDate(reservationDTO.getPaymentDate());
		}
		if (reservationDTO.getPaymentAmount() != null) {
			reservation.setPaymentAmount(new java.math.BigDecimal(reservationDTO.getPaymentAmount()));
		}
		if (reservationDTO.getCreatedAt() != null) {
			reservation.setCreatedAt(reservationDTO.getCreatedAt());
		}
		if (reservationDTO.getUpdatedAt() != null) {
			reservation.setUpdatedAt(reservationDTO.getUpdatedAt());
		}
	}
}
