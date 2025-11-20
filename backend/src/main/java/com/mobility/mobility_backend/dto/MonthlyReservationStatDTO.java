package com.mobility.mobility_backend.dto;

public class MonthlyReservationStatDTO {
    private String month;
    private long reservations;
    private double revenue;

    public MonthlyReservationStatDTO() {}

    public MonthlyReservationStatDTO(String month, long reservations, double revenue) {
        this.month = month;
        this.reservations = reservations;
        this.revenue = revenue;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public long getReservations() {
        return reservations;
    }

    public void setReservations(long reservations) {
        this.reservations = reservations;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }
}
