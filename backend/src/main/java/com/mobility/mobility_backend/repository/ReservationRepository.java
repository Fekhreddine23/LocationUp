package com.mobility.mobility_backend.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobility.mobility_backend.entity.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
	// CORRECTION : Utilisez "user.id" (car User a un champ "id")
	@Query("SELECT r FROM Reservation r WHERE r.user.id = :userId")
	List<Reservation> findByUserId(@Param("userId") Integer userId);

	// Pour les offres
	@Query("SELECT r FROM Reservation r WHERE r.offer.id = :offerId")
	List<Reservation> findByOfferId(@Param("offerId") Integer offerId);

	long countByStatus(Reservation.ReservationStatus status);

	List<Reservation> findTop10ByOrderByReservationDateDesc();

	Optional<Reservation> findByPaymentReference(String paymentReference);

	List<Reservation> findTop5ByPaymentStatusOrderByReservationDateDesc(Reservation.PaymentStatus status);

	List<Reservation> findByPaymentStatusIn(Collection<Reservation.PaymentStatus> statuses);

	List<Reservation> findByPaymentStatusAndReservationDateBetween(Reservation.PaymentStatus status,
			LocalDateTime start, LocalDateTime end);

	List<Reservation> findByPaymentStatusAndUpdatedAtBefore(Reservation.PaymentStatus status,
			LocalDateTime threshold);

}
