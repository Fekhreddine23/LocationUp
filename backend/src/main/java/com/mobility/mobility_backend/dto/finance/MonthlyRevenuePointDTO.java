package com.mobility.mobility_backend.dto.finance;

import java.time.YearMonth;

public class MonthlyRevenuePointDTO {

	private String label;
	private double revenue;
	private long payments;

	public MonthlyRevenuePointDTO() {
	}

	public MonthlyRevenuePointDTO(String label, double revenue, long payments) {
		this.label = label;
		this.revenue = revenue;
		this.payments = payments;
	}

	public MonthlyRevenuePointDTO(YearMonth month, double revenue, long payments) {
		this.label = month.getMonth().name() + " " + month.getYear();
		this.revenue = revenue;
		this.payments = payments;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public double getRevenue() {
		return revenue;
	}

	public void setRevenue(double revenue) {
		this.revenue = revenue;
	}

	public long getPayments() {
		return payments;
	}

	public void setPayments(long payments) {
		this.payments = payments;
	}
}
