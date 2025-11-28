package com.mobility.mobility_backend.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mobility.mobility_backend.dto.ReservationCreationDTO;
import com.mobility.mobility_backend.dto.ReservationDTO;
import com.mobility.mobility_backend.dto.ReservationMapper;
import com.mobility.mobility_backend.dto.timeline.ReservationTimelineDTO;
import com.mobility.mobility_backend.dto.timeline.TimelineEventDTO;
import com.mobility.mobility_backend.entity.Offer;
import com.mobility.mobility_backend.entity.Reservation;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.OfferRepository;
import com.mobility.mobility_backend.repository.ReservationRepository;
import com.mobility.mobility_backend.repository.UserRepository;

@Service
public class ReservationService {

	private final ReservationRepository reservationRepository;
	private final UserRepository userRepository;
	private final OfferRepository offerRepository;
	private final ReservationMapper reservationMapper;
	private final PaymentNotificationService paymentNotificationService;

	@Autowired
	public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository,
			OfferRepository offerRepository, ReservationMapper reservationMapper,
			PaymentNotificationService paymentNotificationService) {
		this.reservationRepository = reservationRepository;
		this.userRepository = userRepository;
		this.offerRepository = offerRepository;
		this.reservationMapper = reservationMapper;
		this.paymentNotificationService = paymentNotificationService;
	}

	// Récupérer toutes les réservations
	public List<ReservationDTO> getAllReservations() {
		System.out.println("🔵 [ReservationService] Getting all reservations");
		List<ReservationDTO> reservations = reservationRepository.findAll().stream().map(reservationMapper::toDTO)
				.collect(Collectors.toList());
		System.out.println("🟢 [ReservationService] Found " + reservations.size() + " reservations");
		return reservations;
	}

	// Récupérer une réservation par ID
	public Optional<ReservationDTO> getReservationById(Integer id) {
		System.out.println("🔵 [ReservationService] Getting reservation by ID: " + id);
		Optional<ReservationDTO> result = reservationRepository.findById(id).map(reservationMapper::toDTO);
		if (result.isPresent()) {
			System.out.println("🟢 [ReservationService] Reservation found: " + result.get().getReservationId());
		} else {
			System.out.println("🔴 [ReservationService] Reservation not found: " + id);
		}
		return result;
	}

	// Récupérer les réservations d'un utilisateur
	public List<ReservationDTO> getReservationsByUserId(Integer userId) {
		System.out.println("🔵 [ReservationService] Getting reservations for user ID: " + userId);

		// Vérifier si l'utilisateur existe
		Optional<User> user = userRepository.findById(userId);
		if (user.isEmpty()) {
			System.out.println("🔴 [ReservationService] User not found with ID: " + userId);
			return List.of();
		}
		System.out.println("🟡 [ReservationService] User found: " + user.get().getUsername());

		List<ReservationDTO> reservations = reservationRepository.findByUserId(userId).stream()
				.map(reservationMapper::toDTO).collect(Collectors.toList());

		System.out.println(
				"🟢 [ReservationService] Found " + reservations.size() + " reservations for user ID: " + userId);
		return reservations;
	}

	// Créer une nouvelle réservation
	public ReservationDTO createReservation(ReservationCreationDTO creationDTO) {
		System.out.println("🔵 [ReservationService] Creating new reservation: " + creationDTO);

		// Validation des relations
		User user = userRepository.findById(creationDTO.getUserId()).orElseThrow(() -> {
			System.out.println("🔴 [ReservationService] User not found with ID: " + creationDTO.getUserId());
			return new RuntimeException("Utilisateur non trouvé");
		});
		System.out.println("🟡 [ReservationService] User found: " + user.getUsername());

		Offer offer = offerRepository.findById(creationDTO.getOfferId()).orElseThrow(() -> {
			System.out.println("🔴 [ReservationService] Offer not found with ID: " + creationDTO.getOfferId());
			return new RuntimeException("Offre non trouvé");
		});
		System.out.println("🟡 [ReservationService] Offer found: " + offer.getOfferId());

		Reservation reservation = new Reservation();
		reservation.setUser(user);
		reservation.setOffer(offer);
		reservation.setReservationDate(creationDTO.getReservationDate());
		reservation.setStatus(Reservation.ReservationStatus.PENDING);
		if (offer.getPrice() != null) {
			reservation.setPaymentAmount(offer.getPrice());
		}
		reservation.setPaymentStatus(Reservation.PaymentStatus.PENDING);
		reservation.setCreatedAt(LocalDateTime.now());
		reservation.setUpdatedAt(LocalDateTime.now());

		System.out.println("🟡 [ReservationService] Saving reservation...");
		Reservation savedReservation = reservationRepository.save(reservation);
		System.out
				.println("🟢 [ReservationService] Reservation created with ID: " + savedReservation.getReservationId());

		return reservationMapper.toDTO(savedReservation);
	}

	// Mettre à jour une réservation
	public Optional<ReservationDTO> updateReservation(Integer id, ReservationDTO reservationDTO) {
		System.out.println("🔵 [ReservationService] Updating reservation ID: " + id);

		return reservationRepository.findById(id).map(existingReservation -> {
			System.out.println(
					"🟡 [ReservationService] Reservation found, current status: " + existingReservation.getStatus());

			reservationMapper.updateEntityFromDTO(reservationDTO, existingReservation);
			Reservation updatedReservation = reservationRepository.save(existingReservation);

			System.out.println(
					"🟢 [ReservationService] Reservation updated, new status: " + updatedReservation.getStatus());
			return reservationMapper.toDTO(updatedReservation);
		});
	}

	// Supprimer une réservation
	public boolean deleteReservation(Integer id) {
		System.out.println("🔵 [ReservationService] Deleting reservation ID: " + id);

		if (reservationRepository.existsById(id)) {
			reservationRepository.deleteById(id);
			System.out.println("🟢 [ReservationService] Reservation deleted: " + id);
			return true;
		}

		System.out.println("🔴 [ReservationService] Reservation not found for deletion: " + id);
		return false;
	}

	// Confirmer une réservation
	public Optional<ReservationDTO> confirmReservation(Integer id) {
		System.out.println("🔵 [ReservationService] Confirming reservation ID: " + id);

		return reservationRepository.findById(id).map(reservation -> {
			System.out.println("🟡 [ReservationService] Reservation found, current status: " + reservation.getStatus());
			System.out.println("🟡 [ReservationService] Reservation user ID: " + reservation.getUser().getId());
			System.out.println("🟡 [ReservationService] Reservation offer ID: " + reservation.getOffer().getOfferId());

			reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
			Reservation updatedReservation = reservationRepository.save(reservation);

			System.out.println(
					"🟢 [ReservationService] Reservation confirmed, new status: " + updatedReservation.getStatus());
			return reservationMapper.toDTO(updatedReservation);
		});
	}

	// Annuler une réservation
	public Optional<ReservationDTO> cancelReservation(Integer id) {
		System.out.println("🔵 [ReservationService] Cancelling reservation ID: " + id);

		return reservationRepository.findById(id).map(reservation -> {
			System.out.println("🟡 [ReservationService] Reservation found, current status: " + reservation.getStatus());

			reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
			Reservation updatedReservation = reservationRepository.save(reservation);

			System.out.println(
					"🟢 [ReservationService] Reservation cancelled, new status: " + updatedReservation.getStatus());
			return reservationMapper.toDTO(updatedReservation);
		});
	}

	public Page<ReservationDTO> getAllReservations(Pageable pageable) {
		Page<Reservation> reservationsPage = reservationRepository.findAll(pageable);

		// Utilisation de votre mapper pour la conversion
		return reservationsPage.map(reservationMapper::toDTO);
	}

	public Page<ReservationDTO> searchReservations(String query, Pageable pageable) {
		if (query == null || query.isBlank()) {
			return getAllReservations(pageable);
		}
		String sanitized = query.trim();
		if (sanitized.matches("\\d+")) {
			Integer id = Integer.valueOf(sanitized);
			Optional<Reservation> reservation = reservationRepository.findById(id);
			if (reservation.isPresent()) {
				return new PageImpl<>(
						Collections.singletonList(reservationMapper.toDTO(reservation.get())), pageable, 1);
			}
			return Page.empty(pageable);
		}
		Page<Reservation> searchResults = reservationRepository.searchByKeyword(sanitized, pageable);
		return searchResults.map(reservationMapper::toDTO);
	}

	public ReservationTimelineDTO getReservationTimeline(Integer reservationId) {
		Reservation reservation = reservationRepository.findById(reservationId)
				.orElseThrow(() -> new IllegalArgumentException("Réservation introuvable"));
		ReservationTimelineDTO timeline = new ReservationTimelineDTO();
		timeline.setReservationId(reservation.getReservationId());
		timeline.setStatus(reservation.getStatus() != null ? reservation.getStatus().name() : null);
		timeline.setPaymentStatus(reservation.getPaymentStatus() != null ? reservation.getPaymentStatus().name() : null);

		List<TimelineEventDTO> events = new java.util.ArrayList<>();
		LocalDateTime created = reservation.getCreatedAt() != null ? reservation.getCreatedAt()
				: (reservation.getReservationDate() != null ? reservation.getReservationDate() : LocalDateTime.now());
		LocalDateTime lastUpdate = reservation.getUpdatedAt() != null ? reservation.getUpdatedAt() : created;
		events.add(new TimelineEventDTO("Réservation créée",
				"La réservation a été enregistrée dans le système", "CREATED", created));

		if (reservation.getReservationDate() != null) {
			events.add(new TimelineEventDTO("Trajet planifié",
					"Départ prévu le " + reservation.getReservationDate().toLocalDate(), "SCHEDULED",
					reservation.getReservationDate()));
		}

		if (reservation.getPaymentStatus() == Reservation.PaymentStatus.PENDING) {
			events.add(new TimelineEventDTO("Paiement en attente",
					"Le paiement doit être confirmé pour finaliser la réservation", "PAYMENT_PENDING", lastUpdate));
		}

		if (reservation.getPaymentStatus() == Reservation.PaymentStatus.REQUIRES_ACTION) {
			events.add(new TimelineEventDTO("Paiement lancé",
					"Session Stripe créée. Finalisez le paiement pour confirmer la réservation.", "PAYMENT_STARTED",
					lastUpdate));
		}

		if (reservation.getPaymentDate() != null
				&& reservation.getPaymentStatus() == Reservation.PaymentStatus.PAID) {
			events.add(new TimelineEventDTO("Paiement confirmé",
					"Paiement reçu via Stripe. Référence: "
							+ (reservation.getPaymentReference() != null ? reservation.getPaymentReference() : "N/A"),
					"PAID", reservation.getPaymentDate()));
		} else if (reservation.getPaymentStatus() == Reservation.PaymentStatus.PAID) {
			events.add(new TimelineEventDTO("Paiement confirmé",
					"Paiement validé. Référence: "
							+ (reservation.getPaymentReference() != null ? reservation.getPaymentReference() : "N/A"),
					"PAID", lastUpdate));
		} else if (reservation.getPaymentStatus() == Reservation.PaymentStatus.FAILED) {
			events.add(new TimelineEventDTO("Paiement échoué",
					"Echec du paiement. Veuillez réessayer ou contacter le support.", "PAYMENT_FAILED", lastUpdate));
		} else if (reservation.getPaymentStatus() == Reservation.PaymentStatus.EXPIRED) {
			events.add(new TimelineEventDTO("Paiement expiré",
					"La session de paiement a expiré. Relancez un paiement pour confirmer la réservation.",
					"PAYMENT_EXPIRED", lastUpdate));
		} else if (reservation.getPaymentStatus() == Reservation.PaymentStatus.REFUNDED) {
			events.add(new TimelineEventDTO("Paiement remboursé",
					"Le paiement a été remboursé sur votre moyen de paiement initial.", "PAYMENT_REFUNDED",
					lastUpdate));
		}

		if (reservation.getStatus() == Reservation.ReservationStatus.CONFIRMED) {
			events.add(new TimelineEventDTO("Réservation confirmée",
					"L'équipe a validé votre réservation.", "CONFIRMED",
					reservation.getUpdatedAt() != null ? reservation.getUpdatedAt() : created));
		} else if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED) {
			events.add(new TimelineEventDTO("Réservation annulée",
					"La réservation a été annulée.", "CANCELLED",
					reservation.getUpdatedAt() != null ? reservation.getUpdatedAt() : created));
		} else if (reservation.getStatus() == Reservation.ReservationStatus.COMPLETED) {
			events.add(new TimelineEventDTO("Service terminé",
					"Votre location est terminée. Merci pour votre confiance.", "COMPLETED",
					reservation.getUpdatedAt() != null ? reservation.getUpdatedAt() : created));
		}

		events.sort((a, b) -> {
			if (a.getTimestamp() == null && b.getTimestamp() == null) {
				return 0;
			}
			if (a.getTimestamp() == null) {
				return 1;
			}
			if (b.getTimestamp() == null) {
				return -1;
			}
			return a.getTimestamp().compareTo(b.getTimestamp());
		});

		timeline.setEvents(events);
		return timeline;
	}

	public Object getReservationStats() {
		System.out.println("📊 Calculating reservation stats...");

		long totalReservations = reservationRepository.count();
		System.out.println("📈 Total reservations: " + totalReservations);

		long pendingReservations = reservationRepository.countByStatus(Reservation.ReservationStatus.PENDING);
		long confirmedReservations = reservationRepository.countByStatus(Reservation.ReservationStatus.CONFIRMED);
		long cancelledReservations = reservationRepository.countByStatus(Reservation.ReservationStatus.CANCELLED);

		System.out.println("📈 Pending: " + pendingReservations);
		System.out.println("📈 Confirmed: " + confirmedReservations);
		System.out.println("📈 Cancelled: " + cancelledReservations);

		Map<String, Object> stats = new HashMap<>();
		stats.put("total", totalReservations);
		stats.put("pending", pendingReservations);
		stats.put("confirmed", confirmedReservations);
		stats.put("cancelled", cancelledReservations);

		double confirmationRate = totalReservations > 0 ? (confirmedReservations * 100.0 / totalReservations) : 0;
		stats.put("confirmationRate", Math.round(confirmationRate * 100.0) / 100.0);

		System.out.println("📊 Final stats: " + stats);
		return stats;
	}

	public List<ReservationDTO> getRecentReservations() {
		List<Reservation> reservations = reservationRepository.findTop10ByOrderByReservationDateDesc();
		return reservations.stream().map(reservationMapper::toDTO).collect(Collectors.toList());
	}

	// update le statut d'une reservation
	// update reservation statue
	public ReservationDTO updateReservationStatus(Integer reservationId, Reservation.ReservationStatus newStatus) {
		Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);

		if (reservationOpt.isPresent()) {
			Reservation reservation = reservationOpt.get();
			reservation.setStatus(newStatus);
			if (newStatus == Reservation.ReservationStatus.CONFIRMED
					|| newStatus == Reservation.ReservationStatus.COMPLETED) {
				reservation.setPaymentStatus(Reservation.PaymentStatus.PAID);
			} else if (newStatus == Reservation.ReservationStatus.CANCELLED) {
				reservation.setPaymentStatus(Reservation.PaymentStatus.FAILED);
			}
			reservation.setUpdatedAt(LocalDateTime.now());

			Reservation savedReservation = reservationRepository.save(reservation);
			if (newStatus == Reservation.ReservationStatus.CONFIRMED
					|| newStatus == Reservation.ReservationStatus.COMPLETED) {
				paymentNotificationService.notifyPaymentSuccess(savedReservation);
			} else if (newStatus == Reservation.ReservationStatus.CANCELLED) {
				paymentNotificationService.notifyPaymentFailure(savedReservation, "Réservation annulée");
			}
			return reservationMapper.toDTO(savedReservation);
		} else {
			throw new RuntimeException("Réservation non trouvée avec l'ID: " + reservationId);
		}
	}

	public ReservationDTO forceExpirePayment(Integer reservationId, String reason) {
		System.out.println("⚠️ [ReservationService] Force expire payment for reservation " + reservationId
				+ (reason != null ? " - reason: " + reason : ""));
		Reservation reservation = reservationRepository.findById(reservationId)
				.orElseThrow(() -> new RuntimeException("Réservation non trouvée avec l'ID: " + reservationId));
		reservation.setPaymentStatus(Reservation.PaymentStatus.EXPIRED);
		reservation.setUpdatedAt(LocalDateTime.now());
		Reservation savedReservation = reservationRepository.save(reservation);
		paymentNotificationService.notifyPaymentExpired(savedReservation);
		return reservationMapper.toDTO(savedReservation);
	}

	public ReservationDTO forceRefundPayment(Integer reservationId, String reason) {
		System.out.println("⚠️ [ReservationService] Force refund payment for reservation " + reservationId
				+ (reason != null ? " - reason: " + reason : ""));
		Reservation reservation = reservationRepository.findById(reservationId)
				.orElseThrow(() -> new RuntimeException("Réservation non trouvée avec l'ID: " + reservationId));
		LocalDateTime now = LocalDateTime.now();
		reservation.setPaymentStatus(Reservation.PaymentStatus.REFUNDED);
		if (reservation.getPaymentDate() == null) {
			reservation.setPaymentDate(now);
		}
		reservation.setUpdatedAt(now);
		Reservation savedReservation = reservationRepository.save(reservation);
		paymentNotificationService.notifyPaymentRefunded(savedReservation, reason);
		return reservationMapper.toDTO(savedReservation);
	}

}
