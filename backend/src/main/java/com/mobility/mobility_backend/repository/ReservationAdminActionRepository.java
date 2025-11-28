package com.mobility.mobility_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobility.mobility_backend.entity.ReservationAdminAction;

public interface ReservationAdminActionRepository extends JpaRepository<ReservationAdminAction, Long> {
	List<ReservationAdminAction> findTop50ByReservationIdOrderByCreatedAtDesc(Integer reservationId);
}
