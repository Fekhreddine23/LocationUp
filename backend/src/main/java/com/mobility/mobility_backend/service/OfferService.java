package com.mobility.mobility_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobility.mobility_backend.dto.OfferDTO;
import com.mobility.mobility_backend.dto.OfferMapper;
import com.mobility.mobility_backend.entity.Offer;
import com.mobility.mobility_backend.repository.OfferRepository;

@Service
@Transactional
public class OfferService {

    private final OfferRepository offerRepository;
    private final OfferMapper offerMapper;

    @Autowired
    public OfferService(OfferRepository offerRepository, OfferMapper offerMapper) {
        this.offerRepository = offerRepository;
        this.offerMapper = offerMapper;
    }

    @Transactional(readOnly = true)
    public List<OfferDTO> getAllOffers() {
        return offerRepository.findAll().stream()
                .map(offerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<OfferDTO> getOfferById(Integer id) {
        return offerRepository.findById(id)
                .map(offerMapper::toDTO);
    }

    public OfferDTO createOffer(OfferDTO offerDTO) {
        validateOffer(offerDTO);
        Offer offer = offerMapper.toEntity(offerDTO);
        Offer savedOffer = offerRepository.save(offer);
        return offerMapper.toDTO(savedOffer);
    }

    public Optional<OfferDTO> updateOffer(Integer id, OfferDTO offerDTO) {
        if (!offerRepository.existsById(id)) {
            return Optional.empty();
        }
        validateOffer(offerDTO);
        Offer offer = offerMapper.toEntity(offerDTO);
        offer.setOfferId(id);
        Offer updatedOffer = offerRepository.save(offer);
        return Optional.of(offerMapper.toDTO(updatedOffer));
    }

    public boolean deleteOffer(Integer id) {
        if (offerRepository.existsById(id)) {
            offerRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private void validateOffer(OfferDTO offerDTO) {
        // Vos règles de validation métier
        if (offerDTO.getPrice() != null && offerDTO.getPrice().signum() <= 0) {
            throw new IllegalArgumentException("Le prix doit être positif");
        }
    }
}