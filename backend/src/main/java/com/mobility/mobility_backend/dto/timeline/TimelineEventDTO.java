package com.mobility.mobility_backend.dto.timeline;

import java.time.LocalDateTime;

public class TimelineEventDTO {

	private String title;
	private String description;
	private String status;
	private LocalDateTime timestamp;

	public TimelineEventDTO() {
	}

	public TimelineEventDTO(String title, String description, String status, LocalDateTime timestamp) {
		this.title = title;
		this.description = description;
		this.status = status;
		this.timestamp = timestamp;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
}
