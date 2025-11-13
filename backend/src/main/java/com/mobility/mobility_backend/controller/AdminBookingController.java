package com.mobility.mobility_backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.dto.ReservationDTO;
import com.mobility.mobility_backend.entity.Reservation;
import com.mobility.mobility_backend.service.ReservationService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookingController {

	private final ReservationService reservationService;

	@Autowired
	public AdminBookingController(ReservationService reservationService) {
		this.reservationService = reservationService;

		System.out.println("✅ AdminBookingController chargé !");
	}

	@GetMapping("/bookings")
	public ResponseEntity<Page<ReservationDTO>> getAllBookings(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ReservationDTO> bookings = reservationService.getAllReservations(pageable);
		return ResponseEntity.ok(bookings);
	}

	/*
	 * @GetMapping("/bookings/stats") public ResponseEntity<?> getBookingStats() {
	 * // Implémentez cette méthode selon vos besoins // Par exemple : nombre de
	 * réservations par statut, revenus, etc. return ResponseEntity.ok().build(); }
	 */

	// temporaire pour mocks des donneees
	@GetMapping("/bookings/stats")
	public ResponseEntity<?> getBookingStats() {
		// TEMPORAIRE - données mock
		Map<String, Object> stats = new HashMap<>();
		stats.put("total", 2);
		stats.put("pending", 1);
		stats.put("confirmed", 1);
		stats.put("cancelled", 0);
		stats.put("confirmationRate", 50.0);

		return ResponseEntity.ok(stats);
	}

	@PostMapping("/bookings/{id}/status")
	public ResponseEntity<?> updateBookingStatus(@PathVariable Integer id,
			@RequestBody Map<String, String> statusUpdate) {

		System.out.println("🎯 ENDPOINT APPELE: POST /api/admin/bookings/" + id + "/status");
		System.out.println("📦 Données reçues: " + statusUpdate);

		String newStatus = statusUpdate.get("status");
		System.out.println("🔄 Mise à jour statut réservation " + id + " vers: " + newStatus);

		try {
			Reservation.ReservationStatus status = Reservation.ReservationStatus.valueOf(newStatus);
			System.out.println("✅ Statut converti: " + status);

			ReservationDTO updatedReservation = reservationService.updateReservationStatus(id, status);
			System.out.println("✅ Réservation mise à jour: " + updatedReservation);

			return ResponseEntity.ok(updatedReservation);

		} catch (IllegalArgumentException e) {
			System.out.println("❌ Statut invalide: " + newStatus);
			return ResponseEntity.badRequest().body("Statut invalide: " + newStatus);
		} catch (Exception e) {
			System.out.println("❌ Erreur: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.notFound().build();
		}
	}

	// 🔄 Annuler une réservation
	@PostMapping("/bookings/{id}/cancel")
	public ResponseEntity<?> cancelBooking(@PathVariable Integer id) {
		System.out.println("🔄 Annulation réservation " + id);

		try {
			ReservationDTO updatedReservation = reservationService.updateReservationStatus(id,
					Reservation.ReservationStatus.CANCELLED);
			return ResponseEntity.ok(updatedReservation);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}

	// ✅ Terminer une réservation
	@PostMapping("/bookings/{id}/complete")
	public ResponseEntity<?> completeBooking(@PathVariable Integer id) {
		System.out.println("✅ Finalisation réservation " + id);

		try {
			ReservationDTO updatedReservation = reservationService.updateReservationStatus(id,
					Reservation.ReservationStatus.COMPLETED);
			return ResponseEntity.ok(updatedReservation);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}

	// 🗑️ Supprimer une réservation
	@DeleteMapping("/bookings/{id}")
	public ResponseEntity<?> deleteBooking(@PathVariable Integer id) {
		System.out.println("🗑️ Suppression réservation " + id);

		try {
			boolean deleted = reservationService.deleteReservation(id);
			if (deleted) {
				return ResponseEntity.ok().body(Map.of("message", "Réservation supprimée avec succès"));
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Erreur lors de la suppression: " + e.getMessage());
		}
	}

}
