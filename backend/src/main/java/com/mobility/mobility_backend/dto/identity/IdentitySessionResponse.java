package com.mobility.mobility_backend.dto.identity;

public class IdentitySessionResponse {

	private String verificationSessionId;
	private String clientSecret;
	private String stripeStatus;

	public IdentitySessionResponse() {
	}

	public IdentitySessionResponse(String verificationSessionId, String clientSecret, String stripeStatus) {
		this.verificationSessionId = verificationSessionId;
		this.clientSecret = clientSecret;
		this.stripeStatus = stripeStatus;
	}

	public String getVerificationSessionId() {
		return verificationSessionId;
	}

	public void setVerificationSessionId(String verificationSessionId) {
		this.verificationSessionId = verificationSessionId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getStripeStatus() {
		return stripeStatus;
	}

	public void setStripeStatus(String stripeStatus) {
		this.stripeStatus = stripeStatus;
	}
}
