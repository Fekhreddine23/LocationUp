package com.mobility.mobility_backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.dto.ReservationDTO;
import com.mobility.mobility_backend.service.ReservationService;

@RestController
@RequestMapping("/api/reservations")
//@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class ReservationControllerTest {

	private final ReservationService reservationService;

	@Autowired
	public ReservationControllerTest(ReservationService reservationService) {
		System.out.println("✅ [ReservationController] Controller initialized and ready!");
		this.reservationService = reservationService;
	}

	// Récupérer toutes les réservations
	@GetMapping
	public ResponseEntity<List<ReservationDTO>> getAllReservations() {
		List<ReservationDTO> reservations = reservationService.getAllReservations();
		return new ResponseEntity<>(reservations, HttpStatus.OK);
	}

	// Récupérer une réservation par ID
	@GetMapping("/{id}")
	public ResponseEntity<ReservationDTO> getReservationById(@PathVariable Integer id) {
		Optional<ReservationDTO> reservation = reservationService.getReservationById(id);
		return reservation.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	// Récupérer les réservations d'un utilisateur
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<ReservationDTO>> getReservationsByUserId(@PathVariable Integer userId) {
		List<ReservationDTO> reservations = reservationService.getReservationsByUserId(userId);
		return new ResponseEntity<>(reservations, HttpStatus.OK);
	}

	// Créer une nouvelle réservation
	@PostMapping
	public ResponseEntity<ReservationDTO> createReservation(@RequestBody ReservationDTO reservationDTO) {
		try {
			ReservationDTO createdReservation = reservationService.createReservation(reservationDTO);
			return new ResponseEntity<>(createdReservation, HttpStatus.CREATED);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	// Mettre à jour une réservation
	@PutMapping("/{id}")
	public ResponseEntity<ReservationDTO> updateReservation(@PathVariable Integer id,
			@RequestBody ReservationDTO reservationDTO) {
		Optional<ReservationDTO> updatedReservation = reservationService.updateReservation(id, reservationDTO);
		return updatedReservation.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	// Supprimer une réservation
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteReservation(@PathVariable Integer id) {
		boolean deleted = reservationService.deleteReservation(id);
		return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Confirmer une réservation
	@PatchMapping("/{id}/confirm")
	public ResponseEntity<ReservationDTO> confirmReservation(@PathVariable Integer id) {
		Optional<ReservationDTO> confirmedReservation = reservationService.confirmReservation(id);
		return confirmedReservation.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	// Annuler une réservation
	@PatchMapping("/{id}/cancel")
	public ResponseEntity<ReservationDTO> cancelReservation(@PathVariable Integer id) {
		Optional<ReservationDTO> cancelledReservation = reservationService.cancelReservation(id);
		return cancelledReservation.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}



	
}