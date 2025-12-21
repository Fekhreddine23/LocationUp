package com.mobility.mobility_backend.dto;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class UserDTO {

	private Integer id;
	private String username;

	@JsonProperty("firstName")
	private String firstname;

	@JsonProperty("lastName")
	private String lastname;
	private String role; // String au lieu de Enum
	private String email;
	private String status;
	private String avatarUrl;

	// Constructeurs
	public UserDTO() {
	}

	public UserDTO(Integer integer, String username, String firstname, String lastname, String role, String email,
			String status) {
		this.id = integer;
		this.username = username;
		this.firstname = firstname;
		this.lastname = lastname;
		this.role = role;
		this.email = email;
		this.status = status;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
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

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return "UserDTO{id=" + id + ", username='" + username + "', email='" + email + "'}";
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

}
