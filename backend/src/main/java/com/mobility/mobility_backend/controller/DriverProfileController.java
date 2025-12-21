package com.mobility.mobility_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.dto.driver.DriverProfileDTO;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.UserRepository;
import com.mobility.mobility_backend.service.DriverProfileService;

@RestController
@RequestMapping("/api/profile/driver")
@PreAuthorize("hasAnyAuthority('ROLE_USER','ROLE_ADMIN')")
public class DriverProfileController {

	private final DriverProfileService driverProfileService;
	private final UserRepository userRepository;

	public DriverProfileController(DriverProfileService driverProfileService, UserRepository userRepository) {
		this.driverProfileService = driverProfileService;
		this.userRepository = userRepository;
	}

	@GetMapping
	public ResponseEntity<DriverProfileDTO> getProfile() {
		Integer currentUserId = resolveCurrentUserId();
		if (currentUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		DriverProfileDTO profile = driverProfileService.getProfile(currentUserId);
		if (profile == null) {
			profile = new DriverProfileDTO();
			profile.setUserId(currentUserId);
		}
		return ResponseEntity.ok(profile);
	}

	@PutMapping
	public ResponseEntity<DriverProfileDTO> saveProfile(@RequestBody DriverProfileDTO profile) {
		Integer currentUserId = resolveCurrentUserId();
		if (currentUserId == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		DriverProfileDTO saved = driverProfileService.saveProfile(currentUserId, profile);
		return ResponseEntity.ok(saved);
	}

	private Integer resolveCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName() == null) {
			return null;
		}
		return userRepository.findByUsername(authentication.getName()).map(User::getId).orElse(null);
	}
}
