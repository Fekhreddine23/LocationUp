package com.mobility.mobility_backend.dto;

public class CityDTO {
    private Long id;
    private String name;
    
    // Constructeurs
    public CityDTO() {}
    
    public CityDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    @Override
    public String toString() {
        return "CityDTO{id=" + id + ", name='" + name + "'}";
    }
}