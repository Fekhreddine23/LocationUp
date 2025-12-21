package com.mobility.mobility_backend.dto.identity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IdentityStatusResponse {

	private String status;
	private boolean verified;
	private String reason;
	private LocalDateTime updatedAt;
	private List<IdentityDocumentDTO> documents = new ArrayList<>();

	public IdentityStatusResponse() {
	}

	public IdentityStatusResponse(String status, boolean verified, String reason, LocalDateTime updatedAt) {
		this.status = status;
		this.verified = verified;
		this.reason = reason;
		this.updatedAt = updatedAt;
	}

	public IdentityStatusResponse(String status, boolean verified, String reason, LocalDateTime updatedAt,
			List<IdentityDocumentDTO> documents) {
		this(status, verified, reason, updatedAt);
		setDocuments(documents);
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

	public List<IdentityDocumentDTO> getDocuments() {
		return documents;
	}

	public void setDocuments(List<IdentityDocumentDTO> documents) {
		this.documents = documents != null ? documents : new ArrayList<>();
	}
}
