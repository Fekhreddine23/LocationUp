package com.mobility.mobility_backend.dto.auth;

public class AuthenticationResponse {
	private String token; // Access token
	private String refreshToken; // Refresh token (non exposé côté front, utilisé pour le cookie)
	private String username;
	private String role;
	private Integer userId;
	private String avatarUrl;

	public AuthenticationResponse() {
	}

	public AuthenticationResponse(String token, String username, String role) {
		this.token = token;
		this.username = username;
		this.role = role;
	}

	public AuthenticationResponse(String token, String username, String role, Integer userId) {
		this.token = token;
		this.username = username;
		this.role = role;
		this.userId = userId;
	}

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

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
