package com.mobility.mobility_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.mobility.mobility_backend.entity.Offer;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateOfferDTO {
	private static final int IMAGE_URL_MAX_LENGTH = 500;

	@NotBlank(message = "Le lieu de départ est requis")
	private String pickupLocationName;

	@NotBlank(message = "Le lieu de retour est requis")
	private String returnLocationName;

	@NotNull(message = "Le service de mobilité est requis")
	private Integer mobilityServiceId; // Gardez l'ID mais alimenté depuis liste

	@NotNull(message = "La date de pickup ne peut pas être nulle")
	@Future(message = "La date de pickup doit être dans le futur")
	private LocalDateTime pickupDatetime;

	@Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
	private String description;

	@NotNull(message = "Le prix ne peut pas être nul")
	@DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être supérieur à 0")
	private BigDecimal price;

	@Size(max = IMAGE_URL_MAX_LENGTH, message = "L'URL de l'image ne peut pas dépasser " + IMAGE_URL_MAX_LENGTH + " caractères")
	private String imageUrl;

	private java.util.List<@Size(max = IMAGE_URL_MAX_LENGTH, message = "Une URL d'image ne peut pas dépasser " + IMAGE_URL_MAX_LENGTH + " caractères") String> galleryUrls;

	private Offer.OfferStatus status = Offer.OfferStatus.PENDING;

	private boolean active = true;

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

	public Integer getMobilityServiceId() {
		return mobilityServiceId;
	}

	public void setMobilityServiceId(Integer mobilityServiceId) {
		this.mobilityServiceId = mobilityServiceId;
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

	public java.util.List<String> getGalleryUrls() {
		return galleryUrls;
	}

	public void setGalleryUrls(java.util.List<String> galleryUrls) {
		this.galleryUrls = galleryUrls;
	}

	public Offer.OfferStatus getStatus() {
		return status;
	}

	public void setStatus(Offer.OfferStatus status) {
		this.status = status;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	// Constructeurs, getters et setters
}
