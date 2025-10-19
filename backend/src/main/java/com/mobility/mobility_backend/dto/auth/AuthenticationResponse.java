package com.mobility.mobility_backend.dto.auth;



public class AuthenticationResponse {
    private String token;
    private String username;
    private String role;

    // Constructeurs
    public AuthenticationResponse() {}

    public AuthenticationResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

    // Getters et Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}