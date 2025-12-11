package com.mobility.mobility_backend.dto.identity;

import java.time.LocalDateTime;

public class IdentityStatusResponse {

	private String status;
	private boolean verified;
	private String reason;
	private LocalDateTime updatedAt;

	public IdentityStatusResponse() {
	}

	public IdentityStatusResponse(String status, boolean verified, String reason, LocalDateTime updatedAt) {
		this.status = status;
		this.verified = verified;
		this.reason = reason;
		this.updatedAt = updatedAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
