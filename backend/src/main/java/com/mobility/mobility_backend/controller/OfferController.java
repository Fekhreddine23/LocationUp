package com.mobility.mobility_backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.dto.OfferDTO;
import com.mobility.mobility_backend.service.OfferService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/offers")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class OfferController {

	private final OfferService offerService;

	@Autowired
	public OfferController(OfferService offerService) {
		this.offerService = offerService;
		// SUPPRIMER offerRepository et offerMapper de l'injection
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

	// Créer une nouvelle offre
	@PostMapping
	public ResponseEntity<OfferDTO> createOffer(@Valid @RequestBody OfferDTO offerDTO) {
		OfferDTO createdOffer = offerService.createOffer(offerDTO);
		return new ResponseEntity<>(createdOffer, HttpStatus.CREATED);
	}

	// Mettre à jour une offre
	@PutMapping("/{id}")
	public ResponseEntity<OfferDTO> updateOffer(@PathVariable Integer id, @Valid @RequestBody OfferDTO offerDTO) {
		Optional<OfferDTO> updatedOffer = offerService.updateOffer(id, offerDTO);
		return updatedOffer.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	// Supprimer une offre
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteOffer(@PathVariable Integer id) {
		boolean deleted = offerService.deleteOffer(id);
		return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}