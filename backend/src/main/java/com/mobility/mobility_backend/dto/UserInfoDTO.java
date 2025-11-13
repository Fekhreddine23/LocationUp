package com.mobility.mobility_backend.dto;

public class UserInfoDTO {

	// DTO simplifié pour les infos utilisateur

	private Long id;
	private String username;
	private String email;

	public UserInfoDTO() {
	}

	public UserInfoDTO(Long id, String username, String email) {
		this.id = id;
		this.username = username;
		this.email = email;
	}

	// Getters et Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

}
