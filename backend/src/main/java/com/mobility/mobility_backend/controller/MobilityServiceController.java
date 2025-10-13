package com.mobility.mobility_backend.controller;

import com.mobility.mobility_backend.dto.MobilityServiceDTO;
import com.mobility.mobility_backend.service.MobilityServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mobility-services")
public class MobilityServiceController {

	@Autowired
	private MobilityServiceService mobilityServiceService;

	/**
	 * Crée un nouveau service de mobilité POST /api/mobility-services
	 */
	@PostMapping
	public ResponseEntity<MobilityServiceDTO> createMobilityService(
			@RequestBody MobilityServiceDTO mobilityServiceDTO) {
		try {
			MobilityServiceDTO createdService = mobilityServiceService
					.createMobilityService(mobilityServiceDTO.getName(), mobilityServiceDTO.getDescription());
			return new ResponseEntity<>(createdService, HttpStatus.CREATED);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Récupère tous les services de mobilité GET /api/mobility-services
	 */
	@GetMapping
	public ResponseEntity<List<MobilityServiceDTO>> getAllMobilityServices() {
		List<MobilityServiceDTO> services = mobilityServiceService.getAllMobilityServices();
		return new ResponseEntity<>(services, HttpStatus.OK);
	}

	/**
	 * Récupère un service de mobilité par son ID GET /api/mobility-services/{id}
	 */
	@GetMapping("/{id}")
	public ResponseEntity<MobilityServiceDTO> getMobilityServiceById(@PathVariable Long id) {
		Optional<MobilityServiceDTO> service = mobilityServiceService.getMobilityServiceById(id);
		return service.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	/**
	 * Récupère un service de mobilité par son nom GET
	 * /api/mobility-services/name/{name}
	 */
	@GetMapping("/name/{name}")
	public ResponseEntity<MobilityServiceDTO> getMobilityServiceByName(@PathVariable String name) {
		Optional<MobilityServiceDTO> service = mobilityServiceService.getMobilityServiceByName(name);
		return service.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	/**
	 * Supprime un service de mobilité par son ID DELETE /api/mobility-services/{id}
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteMobilityService(@PathVariable Long id) {
		boolean deleted = mobilityServiceService.deleteMobilityService(id);
		return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}