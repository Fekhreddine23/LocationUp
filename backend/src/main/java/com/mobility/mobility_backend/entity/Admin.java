package com.mobility.mobility_backend.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "admins")
public class Admin {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "admin_id")
	private Integer adminId;

	@NotBlank(message = "Le nom d'utilisateur est obligatoire")
	@Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
	@Column(unique = true, nullable = false, length = 50)
	private String username;

	@NotBlank(message = "L'email est obligatoire")
	@Email(message = "L'email doit être valide")
	@Column(unique = true, nullable = false, length = 100)
	private String email;

	@NotBlank(message = "Le mot de passe est obligatoire")
	@Column(nullable = false, length = 255)
	private String password;

	@Min(value = 1, message = "Le niveau admin doit être au moins 1")
	@Max(value = 10, message = "Le niveau admin ne peut pas dépasser 10")
	@Column(name = "admin_level", nullable = false)
	private Integer adminLevel;
	
	
	@Column(nullable = false)
    private String role;

	

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// ========== CONSTRUCTEURS ==========
	public Admin() {
	}

	public Admin(String username, String email, String password, Integer adminLevel) {
		this.username = username;
		this.email = email;
		this.password = password;
		this.adminLevel = adminLevel;
	}

	// ========== BUILDER PATTERN ==========
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String username;
		private String email;
		private String password;
		private Integer adminLevel;

		public Builder username(String username) {
			this.username = username;
			return this;
		}

		public Builder email(String email) {
			this.email = email;
			return this;
		}

		public Builder password(String password) {
			this.password = password;
			return this;
		}

		public Builder adminLevel(Integer adminLevel) {
			this.adminLevel = adminLevel;
			return this;
		}

		public Admin build() {
			return new Admin(username, email, password, adminLevel);
		}
	}

	// ========== GETTERS & SETTERS ==========
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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
	
	
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	// ========== EQUALS & HASHCODE ==========
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Admin)) {
			return false;
		}
		Admin admin = (Admin) o;
		return Objects.equals(adminId, admin.adminId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(adminId);
	}

	// ========== TO STRING ==========
	@Override
	public String toString() {
		return "Admin{" + "adminId=" + adminId + ", username='" + username + '\'' + ", email='" + email + '\''
				+ ", adminLevel=" + adminLevel + '}';
	}
}