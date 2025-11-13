package com.mobility.mobility_backend.dto;

import java.time.LocalDateTime;

public class AdminStatsDTO {
	private long totalUsers;
	private long activeUsers;
	private long totalReservations;
	private long pendingReservations;
	private long totalOffers;
	private double totalRevenue;
	private LocalDateTime lastUpdated; // ← AJOUTEZ CE CHAMP

	// Constructeurs
	public AdminStatsDTO() {
		this.lastUpdated = LocalDateTime.now(); // Initialiser avec la date actuelle
	}

	public AdminStatsDTO(long totalUsers, long activeUsers, long totalReservations, long pendingReservations,
			long totalOffers, double totalRevenue) {
		this.totalUsers = totalUsers;
		this.activeUsers = activeUsers;
		this.totalReservations = totalReservations;
		this.pendingReservations = pendingReservations;
		this.totalOffers = totalOffers;
		this.totalRevenue = totalRevenue;
		this.lastUpdated = LocalDateTime.now(); // ← AJOUTEZ DANS CE CONSTRUCTEUR AUSSI
	}

	// NOUVEAU CONSTRUCTEUR AVEC lastUpdated
	public AdminStatsDTO(long totalUsers, long activeUsers, long totalReservations, long pendingReservations,
			long totalOffers, double totalRevenue, LocalDateTime lastUpdated) {
		this.totalUsers = totalUsers;
		this.activeUsers = activeUsers;
		this.totalReservations = totalReservations;
		this.pendingReservations = pendingReservations;
		this.totalOffers = totalOffers;
		this.totalRevenue = totalRevenue;
		this.lastUpdated = lastUpdated;
	}

	// Getters et Setters
	public long getTotalUsers() {
		return totalUsers;
	}

	public void setTotalUsers(long totalUsers) {
		this.totalUsers = totalUsers;
	}

	public long getActiveUsers() {
		return activeUsers;
	}

	public void setActiveUsers(long activeUsers) {
		this.activeUsers = activeUsers;
	}

	public long getTotalReservations() {
		return totalReservations;
	}

	public void setTotalReservations(long totalReservations) {
		this.totalReservations = totalReservations;
	}

	public long getPendingReservations() {
		return pendingReservations;
	}

	public void setPendingReservations(long pendingReservations) {
		this.pendingReservations = pendingReservations;
	}

	public long getTotalOffers() {
		return totalOffers;
	}

	public void setTotalOffers(long totalOffers) {
		this.totalOffers = totalOffers;
	}

	public double getTotalRevenue() {
		return totalRevenue;
	}

	public void setTotalRevenue(double totalRevenue) {
		this.totalRevenue = totalRevenue;
	}

	// AJOUTEZ LE GETTER ET SETTER POUR lastUpdated
	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}