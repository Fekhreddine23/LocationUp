package com.mobility.mobility_backend.dto.identity;

public class IdentityVerificationStatsDTO {
	private long total;
	private long verified;
	private long processing;
	private long requiresInput;
	private long pending;

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public long getVerified() {
		return verified;
	}

	public void setVerified(long verified) {
		this.verified = verified;
	}

	public long getProcessing() {
		return processing;
	}

	public void setProcessing(long processing) {
		this.processing = processing;
	}

	public long getRequiresInput() {
		return requiresInput;
	}

	public void setRequiresInput(long requiresInput) {
		this.requiresInput = requiresInput;
	}

	public long getPending() {
		return pending;
	}

	public void setPending(long pending) {
		this.pending = pending;
	}
}
