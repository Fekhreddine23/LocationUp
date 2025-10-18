package com.mobility.mobility_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobility.mobility_backend.entity.Admin;


@Repository
public interface AdminRepository  extends JpaRepository<Admin, Integer> {

	Optional<Admin> findByUsername(String username);
    Optional<Admin> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

}
