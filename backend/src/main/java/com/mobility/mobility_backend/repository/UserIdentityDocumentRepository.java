package com.mobility.mobility_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobility.mobility_backend.entity.UserIdentityDocument;
import com.mobility.mobility_backend.entity.UserIdentityVerification;

public interface UserIdentityDocumentRepository extends JpaRepository<UserIdentityDocument, Long> {

	List<UserIdentityDocument> findByUser_IdOrderByUpdatedAtDesc(Integer userId);

	List<UserIdentityDocument> findByVerification_IdOrderByUpdatedAtDesc(Long verificationId);

	void deleteByVerification(UserIdentityVerification verification);
}
