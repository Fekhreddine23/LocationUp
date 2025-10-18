package com.mobility.mobility_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateAdminDTO {
	@NotBlank(message = "Le nom d'utilisateur est obligatoire")
	@Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
	private String username;

	@NotBlank(message = "L'email est obligatoire")
	@Email(message = "L'email doit être valide")
	private String email;

	@NotBlank(message = "Le mot de passe est obligatoire")
	@Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
	private String password;

	@NotNull(message = "Le niveau admin est obligatoire")
	@Min(value = 1, message = "Le niveau admin doit être au moins 1")
	@Max(value = 10, message = "Le niveau admin ne peut pas dépasser 10")
	private Integer adminLevel;

	// Constructeurs, Getters, Setters...
	public CreateAdminDTO() {
	}

	public CreateAdminDTO(String username, String email, String password, Integer adminLevel) {
		this.username = username;
		this.email = email;
		this.password = password;
		this.adminLevel = adminLevel;
	}

	// Getters et Setters
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
}