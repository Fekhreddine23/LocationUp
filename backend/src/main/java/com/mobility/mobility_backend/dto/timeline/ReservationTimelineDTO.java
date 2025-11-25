package com.mobility.mobility_backend.dto.timeline;

import java.util.ArrayList;
import java.util.List;

public class ReservationTimelineDTO {

	private Integer reservationId;
	private String status;
	private String paymentStatus;
	private List<TimelineEventDTO> events = new ArrayList<>();

	public Integer getReservationId() {
		return reservationId;
	}

	public void setReservationId(Integer reservationId) {
		this.reservationId = reservationId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public List<TimelineEventDTO> getEvents() {
		return events;
	}

	public void setEvents(List<TimelineEventDTO> events) {
		this.events = events;
	}
}
