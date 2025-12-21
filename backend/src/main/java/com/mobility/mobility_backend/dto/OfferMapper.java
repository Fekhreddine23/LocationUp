package com.mobility.mobility_backend.dto;

// OfferMapper.java - Version avec noms

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobility.mobility_backend.entity.Offer;

@Component
public class OfferMapper {
	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public OfferDTO toDTO(Offer offer) {
		if (offer == null) {
			return null;
		}

		OfferDTO offerDTO = new OfferDTO();
		offerDTO.setOfferId(offer.getOfferId());
		offerDTO.setPickupDatetime(offer.getPickupDatetime());
		offerDTO.setDescription(offer.getDescription());
		offerDTO.setPrice(offer.getPrice());
		offerDTO.setStatus(offer.getStatus());
		offerDTO.setActive(offer.isActive());
		offerDTO.setCreatedAt(offer.getCreatedAt());
		offerDTO.setUpdatedAt(offer.getUpdatedAt());
		offerDTO.setFavorite(false);
		offerDTO.setImageUrl(offer.getImageUrl());
		offerDTO.setGalleryUrls(parseGallery(offer.getGalleryUrls()));

		// IDs des relations
		if (offer.getPickupLocation() != null) {
			offerDTO.setPickupLocationId(offer.getPickupLocation().getCityId());
			offerDTO.setPickupLocationName(offer.getPickupLocation().getName()); // Ajoutez le nom
			offerDTO.setPickupLatitude(offer.getPickupLocation().getLatitude());
			offerDTO.setPickupLongitude(offer.getPickupLocation().getLongitude());
		}
		if (offer.getReturnLocation() != null) {
			offerDTO.setReturnLocationId(offer.getReturnLocation().getCityId());
			offerDTO.setReturnLocationName(offer.getReturnLocation().getName());
			offerDTO.setReturnLatitude(offer.getReturnLocation().getLatitude());
			offerDTO.setReturnLongitude(offer.getReturnLocation().getLongitude());
		}
		if (offer.getMobilityService() != null) {
			offerDTO.setMobilityServiceId(offer.getMobilityService().getServiceId());
			offerDTO.setMobilityServiceName(offer.getMobilityService().getName());
		}
		if (offer.getAdmin() != null) {
			offerDTO.setAdminId(offer.getAdmin().getAdminId());
			offerDTO.setAdminName(offer.getAdmin().getUsername());
		}

		return offerDTO;
	}

	// ... reste des méthodes inchangé

	private java.util.List<String> parseGallery(String stored) {
		if (stored == null || stored.isBlank()) {
			return java.util.Collections.emptyList();
		}
		try {
			if (stored.trim().startsWith("[")) {
				return OBJECT_MAPPER.readValue(stored, new TypeReference<java.util.List<String>>() {});
			}
			return java.util.Arrays.stream(stored.split(","))
					.map(String::trim)
					.filter(s -> !s.isEmpty())
					.filter(this::isSafeImageUrl)
					.limit(5)
					.toList();
		} catch (Exception e) {
			return java.util.Collections.emptyList();
		}
	}

	private boolean isSafeImageUrl(String url) {
		if (url.startsWith("/uploads/")) return true;
		return url.startsWith("http://") || url.startsWith("https://");
	}

}
