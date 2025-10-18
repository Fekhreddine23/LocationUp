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

import com.mobility.mobility_backend.dto.AdminDTO;
import com.mobility.mobility_backend.dto.CreateAdminDTO;
import com.mobility.mobility_backend.service.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

	private final AdminService adminService; // Injection par interface

	@Autowired
	public AdminController(AdminService adminService) {
		this.adminService = adminService;
	}

	@GetMapping
	public ResponseEntity<List<AdminDTO>> getAllAdmins() {
		List<AdminDTO> admins = adminService.getAllAdmins();
		return ResponseEntity.ok(admins);
	}

	@GetMapping("/{id}")
	public ResponseEntity<AdminDTO> getAdminById(@PathVariable Integer id) {
		Optional<AdminDTO> admin = adminService.getAdminById(id);
		return admin.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/username/{username}")
	public ResponseEntity<AdminDTO> getAdminByUsername(@PathVariable String username) {
		Optional<AdminDTO> admin = adminService.getAdminByUsername(username);
		return admin.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<?> createAdmin(@Valid @RequestBody CreateAdminDTO createAdminDTO) {
		try {
			AdminDTO createdAdmin = adminService.createAdmin(createAdminDTO);
			return new ResponseEntity<>(createdAdmin, HttpStatus.CREATED);
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PutMapping("/{id}")
	public ResponseEntity<AdminDTO> updateAdmin(@PathVariable Integer id, @Valid @RequestBody AdminDTO adminDTO) {
		Optional<AdminDTO> updatedAdmin = adminService.updateAdmin(id, adminDTO);
		return updatedAdmin.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteAdmin(@PathVariable Integer id) {
		boolean deleted = adminService.deleteAdmin(id);
		return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
	}

	@GetMapping("/check-username/{username}")
	public ResponseEntity<Boolean> checkUsernameExists(@PathVariable String username) {
		boolean exists = adminService.usernameExists(username);
		return ResponseEntity.ok(exists);
	}

	@GetMapping("/check-email/{email}")
	public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
		boolean exists = adminService.emailExists(email);
		return ResponseEntity.ok(exists);
	}
}