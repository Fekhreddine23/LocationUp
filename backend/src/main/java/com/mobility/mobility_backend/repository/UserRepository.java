package com.mobility.mobility_backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mobility.mobility_backend.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);

	 // Méthode de recherche
    Page<User> findByEmailContainingOrFirstNameContainingOrLastNameContaining(
        String email, String firstName, String lastName, Pageable pageable);

	@Override
	long count();


}
