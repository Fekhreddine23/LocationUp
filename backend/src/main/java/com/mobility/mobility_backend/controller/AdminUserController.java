package com.mobility.mobility_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.mobility.mobility_backend.dto.UserDTO;
import com.mobility.mobility_backend.service.AdminUserService;

@RestController
@RequestMapping("/api/admin/users-management")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

	private final AdminUserService adminUserService;

	@Autowired
	public AdminUserController(AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}

	// GET /api/admin/users - Liste tous les utilisateurs
	@GetMapping
	public ResponseEntity<List<UserDTO>> getAllUsers(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(adminUserService.getAllUsers(page, size));
	}

	// GET /api/admin/users/{id} - Récupère un utilisateur par ID
	@GetMapping("/{id}")
	public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
		return adminUserService.getUserById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	// POST /api/admin/users/{id}/role - Change le rôle d'un utilisateur
	@PostMapping("/{id}/role")
	public ResponseEntity<UserDTO> changeUserRole(@PathVariable Integer id, @RequestBody ChangeRoleRequest request) {
		try {
			UserDTO updatedUser = adminUserService.changeUserRole(id, request.getNewRole());
			return ResponseEntity.ok(updatedUser);
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	// PUT /api/admin/users/{id} - Met à jour un utilisateur
	@PutMapping("/{id}")
	public ResponseEntity<UserDTO> updateUser(@PathVariable Integer id, @RequestBody UserDTO userDTO) {
		try {
			UserDTO updatedUser = adminUserService.updateUser(id, userDTO);
			return ResponseEntity.ok(updatedUser);
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	// POST /api/admin/users/{id}/deactivate - Désactive un utilisateur
	@PostMapping("/{id}/deactivate")
	public ResponseEntity<Void> deactivateUser(@PathVariable Integer id) {
		try {
			adminUserService.deactivateUser(id);
			return ResponseEntity.ok().build();
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	// POST /api/admin/users/{id}/activate - Réactive un utilisateur
	@PostMapping("/{id}/activate")
	public ResponseEntity<Void> activateUser(@PathVariable Integer id) {
		try {
			adminUserService.activateUser(id);
			return ResponseEntity.ok().build();
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	// DELETE /api/admin/users/{id} - Supprime un utilisateur
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
		try {
			adminUserService.deleteUser(id);
			return ResponseEntity.ok().build();
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	// GET /api/admin/users/search - Recherche des utilisateurs
	@GetMapping("/search")
	public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String query,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(adminUserService.searchUsers(query, page, size));
	}

	// DTO pour la requête de changement de rôle
	public static class ChangeRoleRequest {
		private String newRole;

		public String getNewRole() {
			return newRole;
		}

		public void setNewRole(String newRole) {
			this.newRole = newRole;
		}
	}

}
