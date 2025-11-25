package com.mobility.mobility_backend.dto.finance;

import java.time.LocalDateTime;

public class PaymentEventDTO {

	private Long id;
	private String eventId;
	private String reservationReference;
	private String type;
	private String status;
	private String errorMessage;
	private LocalDateTime receivedAt;

	public PaymentEventDTO() {
	}

	public PaymentEventDTO(Long id, String eventId, String reservationReference, String type, String status,
			String errorMessage, LocalDateTime receivedAt) {
		this.id = id;
		this.eventId = eventId;
		this.reservationReference = reservationReference;
		this.type = type;
		this.status = status;
		this.errorMessage = errorMessage;
		this.receivedAt = receivedAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getReservationReference() {
		return reservationReference;
	}

	public void setReservationReference(String reservationReference) {
		this.reservationReference = reservationReference;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public LocalDateTime getReceivedAt() {
		return receivedAt;
	}

	public void setReceivedAt(LocalDateTime receivedAt) {
		this.receivedAt = receivedAt;
	}
}
