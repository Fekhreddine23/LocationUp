package com.mobility.mobility_backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "offers")
public class Offer {

	// Constantes
	public static final int DESCRIPTION_MAX_LENGTH = 1000;
	public static final String PRICE_PRECISION = "10,2";
	public static final int IMAGE_URL_MAX_LENGTH = 500;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "offer_id")
	private Integer offerId;

	@NotNull(message = "Le lieu de départ ne peut pas être nul")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "pickup_location_id", nullable = false)
	private City pickupLocation;

	@NotNull(message = "Le lieu de retour ne peut pas être nul")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "return_location_id", nullable = false)
	private City returnLocation;

	@NotNull(message = "Le service de mobilité ne peut pas être nul")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mobility_service_id", nullable = false)
	private MobilityService mobilityService;

	@NotNull(message = "L'administrateur ne peut pas être nul")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "admin_id", nullable = false)
	private Admin admin;

	@NotNull(message = "La date de pickup ne peut pas être nulle")
	@Future(message = "La date de pickup doit être dans le futur")
	@Column(name = "pickup_datetime", nullable = false)
	private LocalDateTime pickupDatetime;

	@Size(max = DESCRIPTION_MAX_LENGTH, message = "La description ne peut pas dépasser " + DESCRIPTION_MAX_LENGTH
			+ " caractères")
	@Column(columnDefinition = "TEXT")
	private String description;

	@NotNull(message = "Le prix ne peut pas être nul")
	@DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être supérieur à 0")
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;


	@Size(max = IMAGE_URL_MAX_LENGTH, message = "L'URL de l'image ne peut pas dépasser " + IMAGE_URL_MAX_LENGTH + " caractères")
	@Column(name = "image_url", length = IMAGE_URL_MAX_LENGTH)
	private String imageUrl;






	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "status", nullable = false)
	@Enumerated(EnumType.STRING)
	private OfferStatus status = OfferStatus.PENDING; // Valeur par défaut

	@Column(name = "active", nullable = false)
	private boolean active = true; // Valeur par défaut

	@Column(name = "gallery_urls", columnDefinition = "TEXT")
	private String galleryUrls; // JSON array stored as string

	// Constructeurs
	public Offer() {
	}

	// Constructeur avec paramètres
	public Offer(City pickupLocation, City returnLocation, MobilityService mobilityService, Admin admin,
			LocalDateTime pickupDatetime, String description, BigDecimal price, String imageUrl) {
		this.pickupLocation = pickupLocation;
		this.returnLocation = returnLocation;
		this.mobilityService = mobilityService;
		this.admin = admin;
		this.pickupDatetime = pickupDatetime;
		this.description = description;
		this.price = price;
		this.imageUrl = imageUrl;
	}

	// Builder Pattern
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private City pickupLocation;
		private City returnLocation;
		private MobilityService mobilityService;
		private Admin admin;
		private LocalDateTime pickupDatetime;
		private String description;
		private BigDecimal price;
		private String imageUrl;

		public Builder pickupLocation(City pickupLocation) {
			this.pickupLocation = pickupLocation;
			return this;
		}

		public Builder returnLocation(City returnLocation) {
			this.returnLocation = returnLocation;
			return this;
		}

		public Builder mobilityService(MobilityService mobilityService) {
			this.mobilityService = mobilityService;
			return this;
		}

		public Builder admin(Admin admin) {
			this.admin = admin;
			return this;
		}

		public Builder pickupDatetime(LocalDateTime pickupDatetime) {
			this.pickupDatetime = pickupDatetime;
			return this;
		}

		public Builder description(String description) {
			this.description = description;
			return this;
		}

		public Builder price(BigDecimal price) {
			this.price = price;
			return this;
		}

		public Offer build() {
			return new Offer(pickupLocation, returnLocation, mobilityService, admin, pickupDatetime, description,
					price, imageUrl);
		}
	}

	// Getters et Setters
	public Integer getOfferId() {
		return offerId;
	}

	public void setOfferId(Integer offerId) {
		this.offerId = offerId;
	}

	public City getPickupLocation() {
		return pickupLocation;
	}

	public void setPickupLocation(City pickupLocation) {
		this.pickupLocation = pickupLocation;
	}

	public City getReturnLocation() {
		return returnLocation;
	}

	public void setReturnLocation(City returnLocation) {
		this.returnLocation = returnLocation;
	}

	public MobilityService getMobilityService() {
		return mobilityService;
	}

	public void setMobilityService(MobilityService mobilityService) {
		this.mobilityService = mobilityService;
	}

	public Admin getAdmin() {
		return admin;
	}

	public void setAdmin(Admin admin) {
		this.admin = admin;
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}


	public String getImageUrl() {
		return imageUrl;

	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getGalleryUrls() {
		return galleryUrls;
	}

	public void setGalleryUrls(String galleryUrls) {
		this.galleryUrls = galleryUrls;
	}

	// Méthodes utilitaires
	public boolean isAvailable() {
		return pickupDatetime != null && pickupDatetime.isAfter(LocalDateTime.now());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Offer)) {
			return false;
		}
		Offer offer = (Offer) o;
		return offerId != null && Objects.equals(offerId, offer.offerId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(offerId);
	}

	@Override
	public String toString() {
		return "Offer [offerId=" + offerId + ", pickupLocation=" + pickupLocation + ", returnLocation=" + returnLocation
				+ ", mobilityService=" + mobilityService + ", admin=" + admin + ", pickupDatetime=" + pickupDatetime
				+ ", description=" + description + ", price=" + price + ", imageUrl=" + imageUrl + ", createdAt="
				+ createdAt + ", updatedAt=" + updatedAt + ", status=" + status + ", version=" + version + ", active="
				+ active + "]";
	}

	// gestion administrateur
	public OfferStatus getStatus() {
		return status;
	}

	public void setStatus(OfferStatus status) {
		this.status = status;
	}

	public enum OfferStatus {
		PENDING, CONFIRMED, CANCELLED, COMPLETED
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

}
