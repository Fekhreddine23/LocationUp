package com.mobility.mobility_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobility.mobility_backend.entity.PaymentEventLog;

public interface PaymentEventLogRepository extends JpaRepository<PaymentEventLog, Long> {
	List<PaymentEventLog> findTop20ByOrderByReceivedAtDesc();
	List<PaymentEventLog> findTop20ByReservationReferenceOrderByReceivedAtDesc(String reservationReference);
}
