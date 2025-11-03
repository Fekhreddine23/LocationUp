package com.mobility.mobility_backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.dto.CreateOfferDTO;
import com.mobility.mobility_backend.dto.OfferDTO;
import com.mobility.mobility_backend.service.OfferService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/offers")
//@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class OfferController {

    private final OfferService offerService;

    @Autowired
    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    // Récupérer toutes les offres
    @GetMapping
    public ResponseEntity<List<OfferDTO>> getAllOffers() {
        List<OfferDTO> offerDTOs = offerService.getAllOffers();
        return new ResponseEntity<>(offerDTOs, HttpStatus.OK);
    }

    // Récupérer une offre par ID
    @GetMapping("/{id}")
    public ResponseEntity<OfferDTO> getOfferById(@PathVariable Integer id) {
        Optional<OfferDTO> offerDTO = offerService.getOfferById(id);
        return offerDTO.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Créer une nouvelle offre - CORRECTION : Retourne ResponseEntity<?> au lieu de ResponseEntity<OfferDTO>
    @PostMapping
    public ResponseEntity<?> createOffer(@Valid @RequestBody CreateOfferDTO createOfferDTO) {
        try {
            OfferDTO createdOffer = offerService.createOffer(createOfferDTO);
            return new ResponseEntity<>(createdOffer, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de la création: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Mettre à jour une offre - CORRECTION : Retourne ResponseEntity<?>
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOffer(@PathVariable Integer id, @Valid @RequestBody OfferDTO offerDTO) {
        try {
            Optional<OfferDTO> updatedOffer = offerService.updateOffer(id, offerDTO);
            return updatedOffer.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de la mise à jour: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // Supprimer une offre
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Integer id) {
        boolean deleted = offerService.deleteOffer(id);
        return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}