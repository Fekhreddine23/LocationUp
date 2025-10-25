package com.mobility.mobility_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mobility.mobility_backend.dto.UserDTO;
import com.mobility.mobility_backend.dto.UserMapper;
import com.mobility.mobility_backend.entity.Role;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	// Implémentation de loadUserByUsername de UserDetailsService
	 @Override
	    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	        System.out.println("🔍 UserDetailsService loading user: " + username);

	        User user = userRepository.findByUsername(username)
	            .orElseThrow(() -> {
	                System.out.println("❌ User not found in database: " + username);
	                return new UsernameNotFoundException("User not found: " + username);
	            });

	        System.out.println("✅ User found: " + user.getUsername());
	        System.out.println("🔐 Password hash: " + user.getPassword());
	        System.out.println("🎭 Role: " + user.getRole());

	        // ⚠️ CORRECTION CRITIQUE : Convertir le rôle correctement
	        String role = user.getRole().name(); // "ROLE_USER"
	        String simpleRole = role.startsWith("ROLE_") ? role.substring(5) : role; // "USER"

	        System.out.println("👤 Creating UserDetails with role: " + simpleRole);

	        return org.springframework.security.core.userdetails.User.builder()
	            .username(user.getUsername())
	            .password(user.getPassword()) // Le hash BCrypt
	            .roles(simpleRole) // "USER" sans le préfixe ROLE_
	            .build();
	    }

	@Override
	public User createUser(User user) {
		// Vérifier si username ou email existe déjà
		if (userRepository.existsByUsername(user.getUsername())) {
			throw new IllegalArgumentException("Username already exists");
		}
		if (userRepository.existsByEmail(user.getEmail())) {
			throw new IllegalArgumentException("Email already exists");
		}
		return userRepository.save(user);
	}

	@Override
	public User updateUser(Integer id, User user) {
		return userRepository.findById(id).map(existingUser -> {
			existingUser.setUsername(user.getUsername());
			existingUser.setEmail(user.getEmail());
			existingUser.setRole(user.getRole());
			// Ne pas mettre à jour le password ici directement
			return userRepository.save(existingUser);
		}).orElseThrow(() -> new IllegalArgumentException("User not found"));
	}

	@Override
	public Optional<UserDTO> getUserById(Integer id) {
		return userRepository.findById(id).map(UserMapper::toDTO);
	}

	@Override
	public Optional<UserDTO> getUserByUsername(String username) {
		return userRepository.findByUsername(username).map(UserMapper::toDTO);
	}

	@Override
	public Optional<UserDTO> getUserByEmail(String email) {
		return userRepository.findByEmail(email).map(UserMapper::toDTO);
	}

	@Override
	public List<UserDTO> getAllUsers() {
		return userRepository.findAll().stream().map(UserMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public boolean userExists(String username) {
		return userRepository.existsByUsername(username);
	}

	@Override
	public boolean emailExists(String email) {
		return userRepository.existsByEmail(email);
	}

	@Override
	public boolean deleteUser(Integer id) {
		if (userRepository.existsById(id)) {
			userRepository.deleteById(id);
			return true;
		}
		return false;
	}

	@Override
	public UserDTO createUser(String username, String email, String password) {
		// Créer l'entité avec un rôle par défaut
		User user = new User(username, email, password, Role.ROLE_USER); // ← AJOUTER ROLE_USER

		// Réutiliser l'implémentation existante qui vérifie et sauve
		User saved = createUser(user);

		// Mapper en DTO
		UserDTO dto = new UserDTO();
		dto.setId(saved.getId());
		dto.setUsername(saved.getUsername());
		dto.setEmail(saved.getEmail());
		return dto;
	}

	@Override
	public User createUser(String username, String email, String password, Role role) {
		// Vérifier si username ou email existe déjà
		if (userRepository.existsByUsername(username)) {
			throw new IllegalArgumentException("Username already exists");
		}
		if (userRepository.existsByEmail(email)) {
			throw new IllegalArgumentException("Email already exists");
		}

		// Créer et sauvegarder l'utilisateur
		User user = new User(username, email, password, role);
		return userRepository.save(user);
	}

}
