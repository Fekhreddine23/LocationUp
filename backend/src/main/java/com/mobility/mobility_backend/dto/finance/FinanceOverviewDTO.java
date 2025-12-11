package com.mobility.mobility_backend.dto.finance;

import java.util.ArrayList;
import java.util.List;

public class FinanceOverviewDTO {

	private double totalRevenue;
	private double monthToDateRevenue;
	private double outstandingRevenue;
	private double confirmationRate;
	private List<PaymentStatusBreakdownDTO> paymentsByStatus = new ArrayList<>();
	private List<MonthlyRevenuePointDTO> revenueHistory = new ArrayList<>();
	private List<PaymentAlertDTO> alerts = new ArrayList<>();
    private List<OutstandingPointDTO> outstandingByWeek = new ArrayList<>();
    private List<OutstandingPointDTO> outstandingByMonth = new ArrayList<>();
    private long identitiesTotal;
    private long identitiesVerified;
    private long identitiesProcessing;
    private long identitiesRequiresInput;
    private long identitiesPending;

	public double getTotalRevenue() {
		return totalRevenue;
	}

	public void setTotalRevenue(double totalRevenue) {
		this.totalRevenue = totalRevenue;
	}

	public double getMonthToDateRevenue() {
		return monthToDateRevenue;
	}

	public void setMonthToDateRevenue(double monthToDateRevenue) {
		this.monthToDateRevenue = monthToDateRevenue;
	}

	public double getOutstandingRevenue() {
		return outstandingRevenue;
	}

	public void setOutstandingRevenue(double outstandingRevenue) {
		this.outstandingRevenue = outstandingRevenue;
	}

	public List<PaymentStatusBreakdownDTO> getPaymentsByStatus() {
		return paymentsByStatus;
	}

	public void setPaymentsByStatus(List<PaymentStatusBreakdownDTO> paymentsByStatus) {
		this.paymentsByStatus = paymentsByStatus;
	}

	public List<MonthlyRevenuePointDTO> getRevenueHistory() {
		return revenueHistory;
	}

	public void setRevenueHistory(List<MonthlyRevenuePointDTO> revenueHistory) {
		this.revenueHistory = revenueHistory;
	}

	public List<PaymentAlertDTO> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<PaymentAlertDTO> alerts) {
		this.alerts = alerts;
	}

    public double getConfirmationRate() {
        return confirmationRate;
    }

    public void setConfirmationRate(double confirmationRate) {
        this.confirmationRate = confirmationRate;
    }

    public List<OutstandingPointDTO> getOutstandingByWeek() {
        return outstandingByWeek;
    }

    public void setOutstandingByWeek(List<OutstandingPointDTO> outstandingByWeek) {
        this.outstandingByWeek = outstandingByWeek;
    }

    public List<OutstandingPointDTO> getOutstandingByMonth() {
        return outstandingByMonth;
    }

    public void setOutstandingByMonth(List<OutstandingPointDTO> outstandingByMonth) {
        this.outstandingByMonth = outstandingByMonth;
    }

    public long getIdentitiesTotal() {
        return identitiesTotal;
    }

    public void setIdentitiesTotal(long identitiesTotal) {
        this.identitiesTotal = identitiesTotal;
    }

    public long getIdentitiesVerified() {
        return identitiesVerified;
    }

    public void setIdentitiesVerified(long identitiesVerified) {
        this.identitiesVerified = identitiesVerified;
    }

    public long getIdentitiesProcessing() {
        return identitiesProcessing;
    }

    public void setIdentitiesProcessing(long identitiesProcessing) {
        this.identitiesProcessing = identitiesProcessing;
    }

    public long getIdentitiesRequiresInput() {
        return identitiesRequiresInput;
    }

    public void setIdentitiesRequiresInput(long identitiesRequiresInput) {
        this.identitiesRequiresInput = identitiesRequiresInput;
    }

    public long getIdentitiesPending() {
        return identitiesPending;
    }

    public void setIdentitiesPending(long identitiesPending) {
        this.identitiesPending = identitiesPending;
    }
}
