package com.mobility.mobility_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.mobility.mobility_backend.entity.Offer; // Assure-toi que le chemin est correct

public class OfferDTO {

	private Integer offerId;
	private Integer pickupLocationId;
	private Integer returnLocationId;
	private Integer mobilityServiceId;
	private Integer adminId;
	private LocalDateTime pickupDatetime;
	private String description;
	private BigDecimal price;
	private String imageUrl;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private boolean active;
	private Offer.OfferStatus status;

	// Champs ajoutés pour l'affichage
	private String pickupLocationName;
	private String returnLocationName;
	private String mobilityServiceName;
	private String adminName;
	private Double pickupLatitude;
	private Double pickupLongitude;
	private Double returnLatitude;
	private Double returnLongitude;
	private boolean favorite;

	// Constructeurs (tu peux en ajouter selon besoin)
	public OfferDTO() {
	}

	// Getters et Setters existants (générés ou manuels)
	public Integer getOfferId() {
		return offerId;
	}

	public void setOfferId(Integer offerId) {
		this.offerId = offerId;
	}

	public Integer getPickupLocationId() {
		return pickupLocationId;
	}

	public void setPickupLocationId(Integer pickupLocationId) {
		this.pickupLocationId = pickupLocationId;
	}

	public Integer getReturnLocationId() {
		return returnLocationId;
	}

	public void setReturnLocationId(Integer returnLocationId) {
		this.returnLocationId = returnLocationId;
	}

	public Integer getMobilityServiceId() {
		return mobilityServiceId;
	}

	public void setMobilityServiceId(Integer mobilityServiceId) {
		this.mobilityServiceId = mobilityServiceId;
	}

	public Integer getAdminId() {
		return adminId;
	}

	public void setAdminId(Integer adminId) {
		this.adminId = adminId;
	}

	public LocalDateTime getPickupDatetime() {
		return pickupDatetime;
	}

	public void setPickupDatetime(LocalDateTime pickupDatetime) {
		this.pickupDatetime = pickupDatetime;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	
	
	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}


	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Offer.OfferStatus getStatus() {
		return status;
	}

	public void setStatus(Offer.OfferStatus status) {
		this.status = status;
	}

	// Getters et Setters pour les champs d'affichage

	public String getPickupLocationName() {
		return pickupLocationName;
	}

	public void setPickupLocationName(String pickupLocationName) {
		this.pickupLocationName = pickupLocationName;
	}

	public String getReturnLocationName() {
		return returnLocationName;
	}

	public void setReturnLocationName(String returnLocationName) {
		this.returnLocationName = returnLocationName;
	}

	public String getMobilityServiceName() {
		return mobilityServiceName;
	}

	public void setMobilityServiceName(String mobilityServiceName) {
		this.mobilityServiceName = mobilityServiceName;
	}

	public String getAdminName() {
		return adminName;
	}

	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}

	public Double getPickupLatitude() {
		return pickupLatitude;
	}

	public void setPickupLatitude(Double pickupLatitude) {
		this.pickupLatitude = pickupLatitude;
	}

	public Double getPickupLongitude() {
		return pickupLongitude;
	}

	public void setPickupLongitude(Double pickupLongitude) {
		this.pickupLongitude = pickupLongitude;
	}

	public Double getReturnLatitude() {
		return returnLatitude;
	}

	public void setReturnLatitude(Double returnLatitude) {
		this.returnLatitude = returnLatitude;
	}

	public Double getReturnLongitude() {
		return returnLongitude;
	}

	public void setReturnLongitude(Double returnLongitude) {
		this.returnLongitude = returnLongitude;
	}

	public boolean isFavorite() {
		return favorite;
	}

	public void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}

}
