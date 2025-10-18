package com.mobility.mobility_backend.dto;

import java.time.LocalDateTime;

public class AdminDTO {
	private Integer adminId;
	private String username;
	private String email;
	private Integer adminLevel;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	// Constructeurs
	public AdminDTO() {
	}

	public AdminDTO(Integer adminId, String username, String email, Integer adminLevel, LocalDateTime createdAt,
			LocalDateTime updatedAt) {
		this.adminId = adminId;
		this.username = username;
		this.email = email;
		this.adminLevel = adminLevel;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	// Builder
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Integer adminId;
		private String username;
		private String email;
		private Integer adminLevel;
		private LocalDateTime createdAt;
		private LocalDateTime updatedAt;

		public Builder adminId(Integer adminId) {
			this.adminId = adminId;
			return this;
		}

		public Builder username(String username) {
			this.username = username;
			return this;
		}

		public Builder email(String email) {
			this.email = email;
			return this;
		}

		public Builder adminLevel(Integer adminLevel) {
			this.adminLevel = adminLevel;
			return this;
		}

		public Builder createdAt(LocalDateTime createdAt) {
			this.createdAt = createdAt;
			return this;
		}

		public Builder updatedAt(LocalDateTime updatedAt) {
			this.updatedAt = updatedAt;
			return this;
		}

		public AdminDTO build() {
			return new AdminDTO(adminId, username, email, adminLevel, createdAt, updatedAt);
		}
	}

	// Getters et Setters
	public Integer getAdminId() {
		return adminId;
	}

	public void setAdminId(Integer adminId) {
		this.adminId = adminId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getAdminLevel() {
		return adminLevel;
	}

	public void setAdminLevel(Integer adminLevel) {
		this.adminLevel = adminLevel;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}