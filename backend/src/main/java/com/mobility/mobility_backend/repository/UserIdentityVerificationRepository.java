package com.mobility.mobility_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobility.mobility_backend.entity.UserIdentityVerification;

public interface UserIdentityVerificationRepository
		extends JpaRepository<UserIdentityVerification, Long> {

	Optional<UserIdentityVerification> findTopByUser_IdOrderByCreatedAtDesc(Integer userId);

	Optional<UserIdentityVerification> findByStripeSessionId(String sessionId);

	List<UserIdentityVerification> findAllByOrderByUpdatedAtDesc();
}
