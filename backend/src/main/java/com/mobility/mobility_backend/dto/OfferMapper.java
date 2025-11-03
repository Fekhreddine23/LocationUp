package com.mobility.mobility_backend.dto;

// OfferMapper.java - Version avec noms

import org.springframework.stereotype.Component;

import com.mobility.mobility_backend.entity.Offer;



@Component
public class OfferMapper {

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
        offerDTO.setActive(offer.getActive(true));
        offerDTO.setCreatedAt(offer.getCreatedAt());
        offerDTO.setUpdatedAt(offer.getUpdatedAt());

        // IDs des relations
        if (offer.getPickupLocation() != null) {
            offerDTO.setPickupLocationId(offer.getPickupLocation().getCityId());
            offerDTO.setPickupLocationName(offer.getPickupLocation().getName()); // Ajoutez le nom
        }
        if (offer.getReturnLocation() != null) {
            offerDTO.setReturnLocationId(offer.getReturnLocation().getCityId());
            offerDTO.setReturnLocationName(offer.getReturnLocation().getName());
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



}