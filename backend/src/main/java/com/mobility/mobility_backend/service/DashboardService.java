package com.mobility.mobility_backend.service;


import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobility.mobility_backend.dto.AdminStatsDTO;
import com.mobility.mobility_backend.dto.RecentActivityDTO;
import com.mobility.mobility_backend.dto.UserInfoDTO;
import com.mobility.mobility_backend.entity.Reservation;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.OfferRepository;
import com.mobility.mobility_backend.repository.ReservationRepository;
import com.mobility.mobility_backend.repository.UserRepository;

@Service
@Transactional
public class DashboardService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final OfferRepository offerRepository;

    @Autowired
    public DashboardService(UserRepository userRepository,
                          ReservationRepository reservationRepository,
                          OfferRepository offerRepository) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.offerRepository = offerRepository;
    }

    public AdminStatsDTO getAdminStats() {
        // VERSION CORRIGÉE - sans lastLogin
        long totalUsers = userRepository.count();
        long activeUsers = totalUsers; // Pour l'instant, tous les users sont considérés actifs
        long totalReservations = reservationRepository.count();
        long pendingReservations = reservationRepository.countByStatus(Reservation.ReservationStatus.PENDING);
        long totalOffers = offerRepository.count();

        // Revenue - version simplifiée pour démarrer
        double totalRevenue = calculateMockRevenue();

        return new AdminStatsDTO(totalUsers, activeUsers, totalReservations,
                               pendingReservations, totalOffers, totalRevenue);
    }


    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public List<RecentActivityDTO> getRecentActivity() {
        // Récupérer les réservations récentes
        List<Reservation> recentReservations = reservationRepository
            .findTop10ByOrderByReservationDateDesc();

        return recentReservations.stream()
            .map(reservation -> new RecentActivityDTO(
                reservation.getReservationId().longValue(),
                "RESERVATION",
                "Nouvelle réservation créée par " + reservation.getUser().getUsername(),
                reservation.getReservationDate(),
                new UserInfoDTO( // ✅ Utilise UserInfoDTO
                    reservation.getUser().getId().longValue(),
                    reservation.getUser().getUsername(),
                    reservation.getUser().getEmail()
                )
            ))
            .collect(Collectors.toList());
    }


    private double calculateMockRevenue() {
        // Pour l'instant, retourne une valeur mockée
        // Plus tard, tu pourras implémenter la vraie logique
        return 8450.0;
    }
}