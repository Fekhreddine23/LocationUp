package com.mobility.mobility_backend.dto.payment;

public class PaymentSessionResponse {

	private String sessionId;
	private String paymentUrl;

	public PaymentSessionResponse() {
	}

	public PaymentSessionResponse(String sessionId, String paymentUrl) {
		this.sessionId = sessionId;
		this.paymentUrl = paymentUrl;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getPaymentUrl() {
		return paymentUrl;
	}

	public void setPaymentUrl(String paymentUrl) {
		this.paymentUrl = paymentUrl;
	}
}
