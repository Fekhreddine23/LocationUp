package com.mobility.mobility_backend.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import com.mobility.mobility_backend.dto.CreateOfferDTO;
import com.mobility.mobility_backend.dto.OfferDTO;
import com.mobility.mobility_backend.service.OfferService;
import com.mobility.mobility_backend.service.storage.ImageStorageService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOfferController {

	private final OfferService offerService;
	private final ImageStorageService imageStorageService;

	@Autowired
	public AdminOfferController(OfferService offerService, ImageStorageService imageStorageService) {
		this.offerService = offerService;
		this.imageStorageService = imageStorageService;
	}

	// 📋 Récupérer toutes les offres (paginated)
	@GetMapping("/offers")
	public ResponseEntity<Page<OfferDTO>> getAllOffers(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<OfferDTO> offers = offerService.getAllOffers(pageable);
		return ResponseEntity.ok(offers);
	}

	// 🔍 Détail d'une offre
	@GetMapping("/offers/{id}")
	public ResponseEntity<OfferDTO> getOfferById(@PathVariable Integer id) {
		return offerService.getOfferById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
	}

	// 📊 Statistiques des offres
	@GetMapping("/offers/stats")
	public ResponseEntity<?> getOfferStats() {
		Object stats = offerService.getOffersStats();
		return ResponseEntity.ok(stats);
	}

	// 🟢 Activer une offre
	@PostMapping("/offers/{id}/activate")
	public ResponseEntity<?> activateOffer(@PathVariable Integer id) {
		System.out.println("🟢 Activation offre " + id);
		try {
			OfferDTO updatedOffer = offerService.updateOfferStatus(id, true);
			return ResponseEntity.ok(updatedOffer);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}

	// 🔴 Désactiver une offre
	@PostMapping("/offers/{id}/deactivate")
	public ResponseEntity<?> deactivateOffer(@PathVariable Integer id) {
		System.out.println("🔴 Désactivation offre " + id);
		try {
			OfferDTO updatedOffer = offerService.updateOfferStatus(id, false);
			return ResponseEntity.ok(updatedOffer);
		} catch (Exception e) {
			return ResponseEntity.notFound().build();
		}
	}

	// 🗑️ Supprimer une offre (désactivation)
	@DeleteMapping("/offers/{id}")
	public ResponseEntity<?> deleteOffer(@PathVariable Integer id) {
		System.out.println("🗑️ Suppression (désactivation) offre " + id);
		try {
			// Désactive l'offre au lieu de la supprimer
			OfferDTO updatedOffer = offerService.updateOfferStatus(id, false);
			return ResponseEntity.ok().body(Map.of("message", "Offre désactivée avec succès", "offer", updatedOffer));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Erreur lors de la désactivation: " + e.getMessage());
		}
	}

	// ✏️ Modifier une offre
	@PutMapping("/offers/{id}")
	public ResponseEntity<?> updateOffer(@PathVariable Integer id, @RequestBody OfferDTO offerDTO) {
		System.out.println("✏️ Modification offre " + id);
		try {
			Optional<OfferDTO> updatedOffer = offerService.updateOffer(id, offerDTO);
			if (updatedOffer.isPresent()) {
				return ResponseEntity.ok(updatedOffer.get());
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Erreur lors de la modification: " + e.getMessage());
		}
	}

	@PostMapping("/offers")
	public ResponseEntity<?> createOffer(@RequestBody CreateOfferDTO createOfferDTO) {
		System.out.println("➕ Création nouvelle offre");
		System.out.println("📦 Données reçues: " + createOfferDTO);

		try {
			OfferDTO createdOffer = offerService.createOffer(createOfferDTO);
			return ResponseEntity.ok(createdOffer);
		} catch (Exception e) {
			System.out.println("❌ Erreur création: " + e.getMessage());
			return ResponseEntity.badRequest().body("Erreur lors de la création: " + e.getMessage());
		}
	}

	@PostMapping(value = "/offers/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> uploadOfferImage(@RequestParam("file") MultipartFile file) {
		try {
			String url = imageStorageService.storeOfferImage(file);
			return ResponseEntity.ok(Map.of("url", url));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
}
