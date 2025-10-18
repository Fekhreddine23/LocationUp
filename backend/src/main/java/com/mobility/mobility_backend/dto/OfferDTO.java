package com.mobility.mobility_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OfferDTO {
    private Integer offerId;
    private Integer pickupLocationId;    // Doit être Integer
    private Integer returnLocationId;    // Doit être Integer
    private Integer mobilityServiceId;   // Doit être Integer
    private Integer adminId;             // Doit être Integer
    private LocalDateTime pickupDatetime;
    private String description;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructeur complet
    public OfferDTO(Integer offerId, Integer pickupLocationId, Integer returnLocationId,
                   Integer mobilityServiceId, Integer adminId, LocalDateTime pickupDatetime,
                   String description, BigDecimal price, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.offerId = offerId;
        this.pickupLocationId = pickupLocationId;
        this.returnLocationId = returnLocationId;
        this.mobilityServiceId = mobilityServiceId;
        this.adminId = adminId;
        this.pickupDatetime = pickupDatetime;
        this.description = description;
        this.price = price;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructeur vide
    public OfferDTO() {}

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer offerId;
        private Integer pickupLocationId;
        private Integer returnLocationId;
        private Integer mobilityServiceId;
        private Integer adminId;
        private LocalDateTime pickupDatetime;
        private String description;
        private BigDecimal price;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder offerId(Integer offerId) { this.offerId = offerId; return this; }
        public Builder pickupLocationId(Integer pickupLocationId) { this.pickupLocationId = pickupLocationId; return this; }
        public Builder returnLocationId(Integer returnLocationId) { this.returnLocationId = returnLocationId; return this; }
        public Builder mobilityServiceId(Integer mobilityServiceId) { this.mobilityServiceId = mobilityServiceId; return this; }
        public Builder adminId(Integer adminId) { this.adminId = adminId; return this; }
        public Builder pickupDatetime(LocalDateTime pickupDatetime) { this.pickupDatetime = pickupDatetime; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder price(BigDecimal price) { this.price = price; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public OfferDTO build() {
            return new OfferDTO(offerId, pickupLocationId, returnLocationId, mobilityServiceId,
                              adminId, pickupDatetime, description, price, createdAt, updatedAt);
        }
    }

    // Getters et Setters
    public Integer getOfferId() { return offerId; }
    public void setOfferId(Integer offerId) { this.offerId = offerId; }

    public Integer getPickupLocationId() { return pickupLocationId; }
    public void setPickupLocationId(Integer pickupLocationId) { this.pickupLocationId = pickupLocationId; }

    public Integer getReturnLocationId() { return returnLocationId; }
    public void setReturnLocationId(Integer returnLocationId) { this.returnLocationId = returnLocationId; }

    public Integer getMobilityServiceId() { return mobilityServiceId; }
    public void setMobilityServiceId(Integer mobilityServiceId) { this.mobilityServiceId = mobilityServiceId; }

    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }

    public LocalDateTime getPickupDatetime() { return pickupDatetime; }
    public void setPickupDatetime(LocalDateTime pickupDatetime) { this.pickupDatetime = pickupDatetime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}