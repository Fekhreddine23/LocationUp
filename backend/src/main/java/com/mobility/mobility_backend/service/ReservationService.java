package com.mobility.mobility_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
		return reservationRepository.findAll().stream().map(reservationMapper::toDTO).collect(Collectors.toList());
	}

	// Récupérer une réservation par ID
	public Optional<ReservationDTO> getReservationById(Integer id) {
		return reservationRepository.findById(id).map(reservationMapper::toDTO);
	}

	// Récupérer les réservations d'un utilisateur
	public List<ReservationDTO> getReservationsByUserId(Integer userId) {
		return reservationRepository.findByUserId(userId).stream().map(reservationMapper::toDTO)
				.collect(Collectors.toList());
	}

	// Créer une nouvelle réservation
	public ReservationDTO createReservation(ReservationDTO reservationDTO) {
		// Validation des relations
		User user = userRepository.findById(reservationDTO.getUserId())
				.orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

		Offer offer = offerRepository.findById(reservationDTO.getOfferId())
				.orElseThrow(() -> new RuntimeException("Offre non trouvé"));

		Reservation reservation = reservationMapper.toEntity(reservationDTO);
		reservation.setUser(user);
		reservation.setOffer(offer);
		reservation.setStatus(Reservation.ReservationStatus.PENDING);

		Reservation savedReservation = reservationRepository.save(reservation);
		return reservationMapper.toDTO(savedReservation);
	}

	// Mettre à jour une réservation
	public Optional<ReservationDTO> updateReservation(Integer id, ReservationDTO reservationDTO) {
		return reservationRepository.findById(id).map(existingReservation -> {
			reservationMapper.updateEntityFromDTO(reservationDTO, existingReservation);
			Reservation updatedReservation = reservationRepository.save(existingReservation);
			return reservationMapper.toDTO(updatedReservation);
		});
	}

	// Supprimer une réservation
	public boolean deleteReservation(Integer id) {
		if (reservationRepository.existsById(id)) {
			reservationRepository.deleteById(id);
			return true;
		}
		return false;
	}

	// Confirmer une réservation
	public Optional<ReservationDTO> confirmReservation(Integer id) {
		return reservationRepository.findById(id).map(reservation -> {
			reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
			Reservation updatedReservation = reservationRepository.save(reservation);
			return reservationMapper.toDTO(updatedReservation);
		});
	}

	// Annuler une réservation
	public Optional<ReservationDTO> cancelReservation(Integer id) {
		return reservationRepository.findById(id).map(reservation -> {
			reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
			Reservation updatedReservation = reservationRepository.save(reservation);
			return reservationMapper.toDTO(updatedReservation);
		});
	}
}