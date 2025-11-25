package com.mobility.mobility_backend.dto.finance;

import java.util.ArrayList;
import java.util.List;

public class FinanceOverviewDTO {

	private double totalRevenue;
	private double monthToDateRevenue;
	private double outstandingRevenue;
	private List<PaymentStatusBreakdownDTO> paymentsByStatus = new ArrayList<>();
	private List<MonthlyRevenuePointDTO> revenueHistory = new ArrayList<>();
	private List<PaymentAlertDTO> alerts = new ArrayList<>();

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
}
