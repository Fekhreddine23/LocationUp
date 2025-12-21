package com.mobility.mobility_backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.validation.annotation.Validated;

import com.mobility.mobility_backend.dto.UserDTO;
import com.mobility.mobility_backend.dto.user.AvatarResponse;
import com.mobility.mobility_backend.entity.Admin;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.AdminRepository;
import com.mobility.mobility_backend.repository.UserRepository;
import com.mobility.mobility_backend.service.UserAvatarService;
import com.mobility.mobility_backend.service.UserService;

@Validated
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private UserAvatarService userAvatarService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AdminRepository adminRepository;

	/**
	 * Crée un nouvel utilisateur POST /api/users
	 */
	@PostMapping
	public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO, @RequestParam String password) {
		try {
			UserDTO createdUser = userService.createUser(userDTO.getUsername(), userDTO.getEmail(), password);
			return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
		} catch (RuntimeException e) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Récupère tous les utilisateurs GET /api/users
	 */
	@GetMapping
	public ResponseEntity<List<UserDTO>> getAllUsers() {
		List<UserDTO> users = userService.getAllUsers();
		return new ResponseEntity<>(users, HttpStatus.OK);
	}

	/**
	 * Récupère un utilisateur par son ID GET /api/users/{id}
	 */
	@GetMapping("/{id}")
	public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
		Optional<UserDTO> user = userService.getUserById(id);
		return user.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	/**
	 * Récupère un utilisateur par son nom d'utilisateur GET
	 * /api/users/username/{username}
	 */
	@GetMapping("/username/{username}")
	public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
		Optional<UserDTO> user = userService.getUserByUsername(username);
		return user.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	/**
	 * Récupère un utilisateur par son email GET /api/users/email/{email}
	 */
	@GetMapping("/email/{email}")
	public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
		Optional<UserDTO> user = userService.getUserByEmail(email);
		return user.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	/**
	 * Supprime un utilisateur par son ID DELETE /api/users/{id}
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
		boolean deleted = userService.deleteUser(id);
		return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<AvatarResponse> uploadAvatar(@RequestParam("file") MultipartFile file,
			Authentication authentication) {
		AvatarResponse response = resolveOwner(authentication, owner -> {
			if (owner instanceof User user) {
				return userAvatarService.uploadAvatar(user, file);
			} else if (owner instanceof Admin admin) {
				return userAvatarService.uploadAdminAvatar(admin, file);
			}
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable");
		});
		return ResponseEntity.ok(response);
	}

	@GetMapping("/me/avatar")
	public ResponseEntity<AvatarResponse> getCurrentAvatar(Authentication authentication) {
		AvatarResponse response = resolveOwner(authentication, owner -> {
			if (owner instanceof User user) {
				return userAvatarService.getAvatarMetadata(user);
			} else if (owner instanceof Admin admin) {
				return userAvatarService.getAdminAvatarMetadata(admin);
			}
			return null;
		});
		if (response == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/me/avatar")
	public ResponseEntity<Void> deleteAvatar(Authentication authentication) {
		resolveOwner(authentication, owner -> {
			if (owner instanceof User user) {
				userAvatarService.deleteAvatar(user);
			} else if (owner instanceof Admin admin) {
				userAvatarService.deleteAdminAvatar(admin);
			}
			return null;
		});
		return ResponseEntity.noContent().build();
	}

	private <T> T resolveOwner(Authentication authentication, java.util.function.Function<Object, T> handler) {
		if (authentication == null || authentication.getName() == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
		}
		Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
		if (userOpt.isPresent()) {
			return handler.apply(userOpt.get());
		}
		Optional<Admin> adminOpt = adminRepository.findByUsername(authentication.getName());
		if (adminOpt.isPresent()) {
			return handler.apply(adminOpt.get());
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable");
	}
}
