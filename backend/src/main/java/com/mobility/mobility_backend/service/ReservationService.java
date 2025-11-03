package com.mobility.mobility_backend.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mobility.mobility_backend.dto.ReservationDTO;
import com.mobility.mobility_backend.dto.ReservationMapper;
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

	@Autowired
	public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository,
			OfferRepository offerRepository, ReservationMapper reservationMapper) {
		this.reservationRepository = reservationRepository;
		this.userRepository = userRepository;
		this.offerRepository = offerRepository;
		this.reservationMapper = reservationMapper;
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
	public ReservationDTO createReservation(ReservationDTO reservationDTO) {
		System.out.println("🔵 [ReservationService] Creating new reservation: " + reservationDTO);

		// Validation des relations
		User user = userRepository.findById(reservationDTO.getUserId()).orElseThrow(() -> {
			System.out.println("🔴 [ReservationService] User not found with ID: " + reservationDTO.getUserId());
			return new RuntimeException("Utilisateur non trouvé");
		});
		System.out.println("🟡 [ReservationService] User found: " + user.getUsername());

		Offer offer = offerRepository.findById(reservationDTO.getOfferId()).orElseThrow(() -> {
			System.out.println("🔴 [ReservationService] Offer not found with ID: " + reservationDTO.getOfferId());
			return new RuntimeException("Offre non trouvé");
		});
		System.out.println("🟡 [ReservationService] Offer found: " + offer.getOfferId());

		Reservation reservation = reservationMapper.toEntity(reservationDTO);
		reservation.setUser(user);
		reservation.setOffer(offer);
		reservation.setStatus(Reservation.ReservationStatus.PENDING);

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

	    double confirmationRate = totalReservations > 0 ?
	        (confirmedReservations * 100.0 / totalReservations) : 0;
	    stats.put("confirmationRate", Math.round(confirmationRate * 100.0) / 100.0);

	    System.out.println("📊 Final stats: " + stats);
	    return stats;
	}

	 public List<ReservationDTO> getRecentReservations() {
	        List<Reservation> reservations = reservationRepository.findTop10ByOrderByReservationDateDesc();
	        return reservations.stream()
	                .map(reservationMapper::toDTO)
	                .collect(Collectors.toList());
	    }



	 //update le statut d'une reservation
	//update reservation statue
		public ReservationDTO updateReservationStatus(Integer reservationId, Reservation.ReservationStatus newStatus) {
		    Optional<Reservation> reservationOpt = reservationRepository.findById(reservationId);

		    if (reservationOpt.isPresent()) {
		        Reservation reservation = reservationOpt.get();
		        reservation.setStatus(newStatus);

		        Reservation savedReservation = reservationRepository.save(reservation);
		        return reservationMapper.toDTO(savedReservation);
		    } else {
		        throw new RuntimeException("Réservation non trouvée avec l'ID: " + reservationId);
		    }
		}

}