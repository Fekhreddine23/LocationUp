package com.mobility.mobility_backend.dto;

import org.springframework.stereotype.Component;

import com.mobility.mobility_backend.entity.Offer;

@Component
public class OfferMapper {

	public OfferDTO toDTO(Offer offer) {
		if (offer == null) {
			return null;
		}

		OfferDTO dto = new OfferDTO();
		dto.setOfferId(offer.getOfferId());
		dto.setPickupLocationId(
				offer.getPickupLocation() != null ? offer.getPickupLocation().getCityId().intValue() : null);
		dto.setReturnLocationId(
				offer.getReturnLocation() != null ? offer.getReturnLocation().getCityId().intValue() : null);
		dto.setMobilityServiceId(
				offer.getMobilityService() != null ? offer.getMobilityService().getServiceId().intValue() : null);
		dto.setAdminId(offer.getAdmin() != null ? offer.getAdmin().getAdminId().intValue() : null);
		dto.setPickupDatetime(offer.getPickupDatetime());
		dto.setDescription(offer.getDescription());
		dto.setPrice(offer.getPrice());
		dto.setCreatedAt(offer.getCreatedAt());
		dto.setUpdatedAt(offer.getUpdatedAt());

		return dto;
	}

	public Offer toEntity(OfferDTO offerDTO) {
		if (offerDTO == null) {
			return null;
		}

		// Note: Les relations ne sont pas définies ici, seulement les champs simples
		Offer offer = new Offer();
		offer.setOfferId(offerDTO.getOfferId());
		offer.setPickupDatetime(offerDTO.getPickupDatetime());
		offer.setDescription(offerDTO.getDescription());
		offer.setPrice(offerDTO.getPrice());

		return offer;
	}

	public void updateEntityFromDTO(OfferDTO offerDTO, Offer offer) {
		if (offerDTO == null || offer == null) {
			return;
		}

		if (offerDTO.getPickupDatetime() != null) {
			offer.setPickupDatetime(offerDTO.getPickupDatetime());
		}
		if (offerDTO.getDescription() != null) {
			offer.setDescription(offerDTO.getDescription());
		}
		if (offerDTO.getPrice() != null) {
			offer.setPrice(offerDTO.getPrice());
		}
	}
}