package com.mobility.mobility_backend.dto.auth;

public class AuthenticationResponse {
	private String token;
	private String username;
	private String role;
	 private Integer userId;

	// Constructeurs
	public AuthenticationResponse() {
	}

	public AuthenticationResponse(String token, String username, String role) {
		this.token = token;
		this.username = username;
		this.role = role;
	}

	// NOUVEAU Constructeur avec userId
    public AuthenticationResponse(String token, String username, String role, Integer userId) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.setUserId(userId); // ← MÊME TYPE QUE User.id
    }



	// Getters et Setters
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}


}