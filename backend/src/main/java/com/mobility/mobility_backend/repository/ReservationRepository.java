package com.mobility.mobility_backend.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mobility.mobility_backend.dto.finance.PeriodAmountProjection;
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

	@Query("SELECT COALESCE(SUM(COALESCE(r.paymentAmount, r.offer.price, 0)), 0) FROM Reservation r "
		+ "WHERE r.paymentStatus = com.mobility.mobility_backend.entity.Reservation$PaymentStatus.PAID")
	BigDecimal sumPaidAmount();

	@Query("SELECT COALESCE(SUM(COALESCE(r.paymentAmount, r.offer.price, 0)), 0) FROM Reservation r "
		+ "WHERE r.paymentStatus IN (com.mobility.mobility_backend.entity.Reservation$PaymentStatus.PENDING, "
		+ "com.mobility.mobility_backend.entity.Reservation$PaymentStatus.REQUIRES_ACTION)")
	BigDecimal sumOutstandingAmount();

	@Query("SELECT COALESCE(SUM(COALESCE(r.paymentAmount, r.offer.price, 0)), 0) FROM Reservation r "
		+ "WHERE r.paymentStatus = com.mobility.mobility_backend.entity.Reservation$PaymentStatus.PAID "
		+ "AND r.paymentDate >= :start")
	BigDecimal sumPaidAmountSince(@Param("start") LocalDateTime start);

	@Query(value = "SELECT FORMATDATETIME(r.reservation_date, 'YYYY-ww') AS period, COUNT(*) AS count, "
		+ "COALESCE(SUM(COALESCE(r.payment_amount, o.price, 0)), 0) AS amount "
		+ "FROM reservations r LEFT JOIN offers o ON o.offer_id = r.offer_id "
		+ "WHERE r.payment_status IN ('PENDING','REQUIRES_ACTION') AND r.reservation_date >= :from "
		+ "GROUP BY FORMATDATETIME(r.reservation_date, 'YYYY-ww') ORDER BY period",
		nativeQuery = true)
	List<PeriodAmountProjection> findOutstandingByWeek(@Param("from") LocalDateTime from);

	@Query(value = "SELECT FORMATDATETIME(r.reservation_date, 'YYYY-MM') AS period, COUNT(*) AS count, "
		+ "COALESCE(SUM(COALESCE(r.payment_amount, o.price, 0)), 0) AS amount "
		+ "FROM reservations r LEFT JOIN offers o ON o.offer_id = r.offer_id "
		+ "WHERE r.payment_status IN ('PENDING','REQUIRES_ACTION') AND r.reservation_date >= :from "
		+ "GROUP BY FORMATDATETIME(r.reservation_date, 'YYYY-MM') ORDER BY period",
		nativeQuery = true)
	List<PeriodAmountProjection> findOutstandingByMonth(@Param("from") LocalDateTime from);

	@Query("SELECT r FROM Reservation r LEFT JOIN r.user u LEFT JOIN r.offer o "
			+ "WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) "
			+ "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) "
			+ "OR LOWER(o.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	Page<Reservation> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

}
