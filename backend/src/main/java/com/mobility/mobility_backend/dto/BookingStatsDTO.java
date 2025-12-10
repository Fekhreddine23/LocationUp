package com.mobility.mobility_backend.dto;

public class BookingStatsDTO {

	private long totalBookings;
	private long pendingBookings;
	private long confirmedBookings;
	private long cancelledBookings;
	private long completedBookings;
	private double totalRevenue;
	private double outstandingRevenue;
	private double monthToDateRevenue;
	private double confirmationRate;

	public long getTotalBookings() {
		return totalBookings;
	}

	public void setTotalBookings(long totalBookings) {
		this.totalBookings = totalBookings;
	}

	public long getPendingBookings() {
		return pendingBookings;
	}

	public void setPendingBookings(long pendingBookings) {
		this.pendingBookings = pendingBookings;
	}

	public long getConfirmedBookings() {
		return confirmedBookings;
	}

	public void setConfirmedBookings(long confirmedBookings) {
		this.confirmedBookings = confirmedBookings;
	}

	public long getCancelledBookings() {
		return cancelledBookings;
	}

	public void setCancelledBookings(long cancelledBookings) {
		this.cancelledBookings = cancelledBookings;
	}

	public long getCompletedBookings() {
		return completedBookings;
	}

	public void setCompletedBookings(long completedBookings) {
		this.completedBookings = completedBookings;
	}

	public double getTotalRevenue() {
		return totalRevenue;
	}

	public void setTotalRevenue(double totalRevenue) {
		this.totalRevenue = totalRevenue;
	}

	public double getOutstandingRevenue() {
		return outstandingRevenue;
	}

	public void setOutstandingRevenue(double outstandingRevenue) {
		this.outstandingRevenue = outstandingRevenue;
	}

	public double getMonthToDateRevenue() {
		return monthToDateRevenue;
	}

	public void setMonthToDateRevenue(double monthToDateRevenue) {
		this.monthToDateRevenue = monthToDateRevenue;
	}

	public double getConfirmationRate() {
		return confirmationRate;
	}

	public void setConfirmationRate(double confirmationRate) {
		this.confirmationRate = confirmationRate;
	}
}
