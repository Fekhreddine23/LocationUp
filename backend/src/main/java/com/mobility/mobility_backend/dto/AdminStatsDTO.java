package com.mobility.mobility_backend.dto;

public class AdminStatsDTO {
    private long totalUsers;
    private long activeUsers;
    private long totalReservations;
    private long pendingReservations;
    private long totalOffers;
    private double totalRevenue;

    // Constructeurs
    public AdminStatsDTO() {}

    public AdminStatsDTO(long totalUsers, long activeUsers, long totalReservations,
                        long pendingReservations, long totalOffers, double totalRevenue) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.totalReservations = totalReservations;
        this.pendingReservations = pendingReservations;
        this.totalOffers = totalOffers;
        this.totalRevenue = totalRevenue;
    }

    // Getters et Setters
    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

    public long getTotalReservations() { return totalReservations; }
    public void setTotalReservations(long totalReservations) { this.totalReservations = totalReservations; }

    public long getPendingReservations() { return pendingReservations; }
    public void setPendingReservations(long pendingReservations) { this.pendingReservations = pendingReservations; }

    public long getTotalOffers() { return totalOffers; }
    public void setTotalOffers(long totalOffers) { this.totalOffers = totalOffers; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
}