package com.mobility.mobility_backend.dto.payment;

public class PaymentStatusResponse {

	private Integer reservationId;
	private String paymentStatus;
	private String stripeStatus;
	private boolean updated;

	public PaymentStatusResponse() {
	}

	public PaymentStatusResponse(Integer reservationId, String paymentStatus, String stripeStatus, boolean updated) {
		this.reservationId = reservationId;
		this.paymentStatus = paymentStatus;
		this.stripeStatus = stripeStatus;
		this.updated = updated;
	}

	public Integer getReservationId() {
		return reservationId;
	}

	public void setReservationId(Integer reservationId) {
		this.reservationId = reservationId;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getStripeStatus() {
		return stripeStatus;
	}

	public void setStripeStatus(String stripeStatus) {
		this.stripeStatus = stripeStatus;
	}

	public boolean isUpdated() {
		return updated;
	}

	public void setUpdated(boolean updated) {
		this.updated = updated;
	}
}
