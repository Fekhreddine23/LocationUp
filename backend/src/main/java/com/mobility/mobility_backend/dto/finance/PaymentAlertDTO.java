package com.mobility.mobility_backend.dto.finance;

import java.time.LocalDateTime;

public class PaymentAlertDTO {

	private Integer reservationId;
	private String customer;
	private double amount;
	private String paymentStatus;
	private String severity;
	private String message;
	private LocalDateTime reservationDate;
	private boolean identityVerified;
	private String identityStatus;

	public PaymentAlertDTO() {
	}

	public PaymentAlertDTO(Integer reservationId, String customer, double amount, String paymentStatus, String severity,
			String message, LocalDateTime reservationDate) {
		this.reservationId = reservationId;
		this.customer = customer;
		this.amount = amount;
		this.paymentStatus = paymentStatus;
		this.severity = severity;
		this.message = message;
		this.reservationDate = reservationDate;
	}
	public PaymentAlertDTO(Integer reservationId, String customer, double amount, String paymentStatus, String severity,
			String message, LocalDateTime reservationDate, boolean identityVerified, String identityStatus) {
		this(reservationId, customer, amount, paymentStatus, severity, message, reservationDate);
		this.identityVerified = identityVerified;
		this.identityStatus = identityStatus;
	}

	public Integer getReservationId() {
		return reservationId;
	}

	public void setReservationId(Integer reservationId) {
		this.reservationId = reservationId;
	}

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LocalDateTime getReservationDate() {
		return reservationDate;
	}

	public void setReservationDate(LocalDateTime reservationDate) {
		this.reservationDate = reservationDate;
	}

	public boolean isIdentityVerified() {
		return identityVerified;
	}

	public void setIdentityVerified(boolean identityVerified) {
		this.identityVerified = identityVerified;
	}

	public String getIdentityStatus() {
		return identityStatus;
	}

	public void setIdentityStatus(String identityStatus) {
		this.identityStatus = identityStatus;
	}
}
