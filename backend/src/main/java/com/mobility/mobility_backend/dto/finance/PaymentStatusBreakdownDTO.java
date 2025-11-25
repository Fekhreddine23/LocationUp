package com.mobility.mobility_backend.dto.finance;

public class PaymentStatusBreakdownDTO {

	private String status;
	private long count;
	private double amount;

	public PaymentStatusBreakdownDTO() {
	}

	public PaymentStatusBreakdownDTO(String status, long count, double amount) {
		this.status = status;
		this.count = count;
		this.amount = amount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}
}
