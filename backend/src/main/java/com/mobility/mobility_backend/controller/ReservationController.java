package com.mobility.mobility_backend.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
public class ReservationController {

	private final ReservationService reservationService;

	@Autowired
	public ReservationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}
	
	//tests
		// ⚠️ ASSURE-TOI QUE CETTE MÉTHODE EXISTE BIEN
	    @PostMapping("/test-public")
	    public ResponseEntity<Map<String, Object>> testPublic() {
	        System.out.println("✅ [ReservationController] PUBLIC POST endpoint called!");
	        return ResponseEntity.ok(Map.of(
	            "message", "ReservationController public POST endpoint works!",
	            "timestamp", LocalDateTime.now().toString(),
	            "controller", "ReservationController"
	        ));
	    }

	 // Et aussi la méthode GET
	    @GetMapping("/test-simple")
	    public ResponseEntity<String> testSimple() {
	        System.out.println("✅ [ReservationController] GET endpoint called!");
	        return ResponseEntity.ok("GET endpoint works!");
	    }




		@PostMapping("/test-auth")
		public ResponseEntity<?> testAuthEndpoint() {
		    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		    System.out.println("✅ [ReservationController] AUTHENTICATED test endpoint called");
		    System.out.println("🔐 Authenticated user: " + (auth != null ? auth.getName() : "null"));
		    System.out.println("🔐 User authorities: " + (auth != null ? auth.getAuthorities() : "null"));

		    if (auth == null || !auth.isAuthenticated()) {
		        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not authenticated"));
		    }

		    return ResponseEntity.ok(Map.of(
		        "message", "Authenticated endpoint works!",
		        "user", auth.getName(),
		        "authorities", auth.getAuthorities().stream()
		                          .map(GrantedAuthority::getAuthority)
		                          .collect(Collectors.toList()),
		        "timestamp", LocalDateTime.now().toString()
		    ));
		}
	
	
	
	

	// Récupérer toutes les réservations
	@GetMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<List<ReservationDTO>> getAllReservations() {
		List<ReservationDTO> reservations = reservationService.getAllReservations();
		return new ResponseEntity<>(reservations, HttpStatus.OK);
	}

	// Récupérer une réservation par ID
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ReservationDTO> getReservationById(@PathVariable Integer id) {
		Optional<ReservationDTO> reservation = reservationService.getReservationById(id);
		return reservation.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	// Récupérer les réservations d'un utilisateur
	@GetMapping("/user/{userId}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<List<ReservationDTO>> getReservationsByUserId(@PathVariable Integer userId) {
		List<ReservationDTO> reservations = reservationService.getReservationsByUserId(userId);
		return new ResponseEntity<>(reservations, HttpStatus.OK);
	}

	// Créer une nouvelle réservation
	@PostMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ReservationDTO> updateReservation(@PathVariable Integer id,
			@RequestBody ReservationDTO reservationDTO) {
		Optional<ReservationDTO> updatedReservation = reservationService.updateReservation(id, reservationDTO);
		return updatedReservation.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	// Supprimer une réservation
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<Void> deleteReservation(@PathVariable Integer id) {
		boolean deleted = reservationService.deleteReservation(id);
		return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	// Confirmer une réservation
	@PatchMapping("/{id}/confirm")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ReservationDTO> confirmReservation(@PathVariable Integer id) {
		Optional<ReservationDTO> confirmedReservation = reservationService.confirmReservation(id);
		return confirmedReservation.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	// Annuler une réservation
	@PatchMapping("/{id}/cancel")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	public ResponseEntity<ReservationDTO> cancelReservation(@PathVariable Integer id) {
		Optional<ReservationDTO> cancelledReservation = reservationService.cancelReservation(id);
		return cancelledReservation.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}
}