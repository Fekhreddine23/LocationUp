package com.mobility.mobility_backend.dto;

import java.time.LocalDateTime;

public class RecentActivityDTO {

	private Long id;
    private String type; // "RESERVATION", "USER", "OFFER"
    private String description;
    private LocalDateTime timestamp;
    private UserInfoDTO user;

    // Constructeurs, Getters, Setters...
    public RecentActivityDTO() {}

    public RecentActivityDTO(Long id, String type, String description, LocalDateTime timestamp, UserInfoDTO user) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.timestamp = timestamp;
        this.user = user;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public UserInfoDTO getUser() { return user; }
    public void setUser(UserInfoDTO user) { this.user = user; }
}




