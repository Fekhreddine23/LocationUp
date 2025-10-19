package com.mobility.mobility_backend.dto;

import org.springframework.stereotype.Component;

@Component
public class UserDTO {

	private Integer id;
	private String username;
	private String email;

	// Constructeurs
	public UserDTO() {
	}

	public UserDTO(Integer integer, String username, String email) {
		this.id = integer;
		this.username = username;
		this.email = email;
	}

	// Getters et Setters
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
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

	@Override
	public String toString() {
		return "UserDTO{id=" + id + ", username='" + username + "', email='" + email + "'}";
	}

}
