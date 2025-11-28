package com.mobility.mobility_backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.dto.ReservationDTO;
import com.mobility.mobility_backend.dto.finance.PaymentEventDTO;
import com.mobility.mobility_backend.dto.payment.AdminPaymentSessionRequest;
import com.mobility.mobility_backend.dto.payment.PaymentAdminActionRequest;
import com.mobility.mobility_backend.dto.payment.PaymentSessionRequest;
import com.mobility.mobility_backend.dto.payment.PaymentSessionResponse;
import com.mobility.mobility_backend.entity.Reservation;
import com.mobility.mobility_backend.service.FinanceService;
import com.mobility.mobility_backend.entity.ReservationAdminAction;
import com.mobility.mobility_backend.service.FinanceService;
import com.mobility.mobility_backend.service.PaymentService;
import com.mobility.mobility_backend.service.ReservationAdminActionService;
import com.mobility.mobility_backend.service.ReservationService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookingController {

	private final ReservationService reservationService;
	private final PaymentService paymentService;
	private final FinanceService financeService;
	private final ReservationAdminActionService adminActionService;

	@Autowired
	public AdminBookingController(ReservationService reservationService, PaymentService paymentService,
			FinanceService financeService, ReservationAdminActionService adminActionService) {
		this.reservationService = reservationService;
		this.paymentService = paymentService;
		this.financeService = financeService;
		this.adminActionService = adminActionService;

		System.out.println("✅ AdminBookingController chargé !");
	}

	@GetMapping("/bookings")
	public ResponseEntity<Page<ReservationDTO>> getAllBookings(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ReservationDTO> bookings = reservationService.getAllReservations(pageable);
		return ResponseEntity.ok(bookings);
	}

	@GetMapping("/bookings/{id}")
	public ResponseEntity<ReservationDTO> getBookingById(@PathVariable Integer id) {
		return reservationService.getReservationById(id).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
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

	@GetMapping("/bookings/search")
	public ResponseEntity<Page<ReservationDTO>> searchBookings(@RequestParam String query,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ReservationDTO> results = reservationService.searchReservations(query, pageable);
		return ResponseEntity.ok(results);
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
			adminActionService.recordAction(id, currentAdmin(), "STATUS_UPDATE", "Nouveau statut: " + status.name());
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
			adminActionService.recordAction(id, currentAdmin(), "CANCELLED", "Annulation manuelle");
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
			adminActionService.recordAction(id, currentAdmin(), "COMPLETED", "Finalisation manuelle");
			return ResponseEntity.ok(updatedReservation);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/bookings/{id}/payment/expire")
	public ResponseEntity<?> forcePaymentExpiration(@PathVariable Integer id,
			@RequestBody(required = false) PaymentAdminActionRequest request) {
		System.out.println("⚠️ Force expire paiement pour réservation " + id + " via endpoint admin");
		try {
			ReservationDTO updatedReservation = reservationService.forceExpirePayment(id,
					request != null ? request.getReason() : null);
			adminActionService.recordAction(id, currentAdmin(), "PAYMENT_EXPIRE",
					"Motif: " + (request != null ? request.getReason() : "—"));
			return ResponseEntity.ok(updatedReservation);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping("/bookings/{id}/payment/refund")
	public ResponseEntity<?> forcePaymentRefund(@PathVariable Integer id,
			@RequestBody(required = false) PaymentAdminActionRequest request) {
		System.out.println("⚠️ Force refund paiement pour réservation " + id + " via endpoint admin");
		try {
			ReservationDTO updatedReservation = reservationService.forceRefundPayment(id,
					request != null ? request.getReason() : null);
			adminActionService.recordAction(id, currentAdmin(), "PAYMENT_REFUND",
					"Motif: " + (request != null ? request.getReason() : "—"));
			return ResponseEntity.ok(updatedReservation);
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping("/bookings/{id}/payment-session")
	public ResponseEntity<?> createPaymentSession(@PathVariable Integer id,
			@RequestBody(required = false) AdminPaymentSessionRequest request) {
		try {
			PaymentSessionRequest sessionRequest = new PaymentSessionRequest();
			sessionRequest.setReservationId(id);
			if (request != null) {
				sessionRequest.setSuccessUrl(request.getSuccessUrl());
				sessionRequest.setCancelUrl(request.getCancelUrl());
			}
			PaymentSessionResponse response = paymentService.createCheckoutSession(sessionRequest);
			return ResponseEntity.ok(response);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	// 🗑️ Supprimer une réservation
	@DeleteMapping("/bookings/{id}")
	public ResponseEntity<?> deleteBooking(@PathVariable Integer id) {
		System.out.println("🗑️ Suppression réservation " + id);

		try {
			boolean deleted = reservationService.deleteReservation(id);
			if (deleted) {
				adminActionService.recordAction(id, currentAdmin(), "DELETED", "Réservation supprimée");
				return ResponseEntity.ok().body(Map.of("message", "Réservation supprimée avec succès"));
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Erreur lors de la suppression: " + e.getMessage());
		}
	}

	@GetMapping("/bookings/{id}/payment/events")
	public ResponseEntity<?> getPaymentEvents(@PathVariable Integer id) {
		try {
			return ResponseEntity.ok(financeService.getReservationPaymentEvents(id));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/bookings/{id}/admin-actions")
	public ResponseEntity<?> getAdminActions(@PathVariable Integer id) {
		try {
			List<ReservationAdminAction> actions = adminActionService.getActions(id);
			return ResponseEntity.ok(actions);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	private String currentAdmin() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth != null ? auth.getName() : "unknown";
	}

}
