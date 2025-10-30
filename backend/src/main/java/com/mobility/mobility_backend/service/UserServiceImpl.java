package com.mobility.mobility_backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mobility.mobility_backend.dto.UserDTO;
import com.mobility.mobility_backend.dto.UserMapper;
import com.mobility.mobility_backend.entity.Admin;
import com.mobility.mobility_backend.entity.Role;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.AdminRepository;
import com.mobility.mobility_backend.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AdminRepository adminRepository;

	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	// Implémentation de loadUserByUsername de UserDetailsService
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("🔍 [UserDetailsService] Loading user: " + username);

		// 1. Chercher d'abord dans la table users
		Optional<User> user = userRepository.findByUsername(username);
		if (user.isPresent()) {
			System.out.println("✅ [UserDetailsService] Found in USERS table: " + username);
			System.out.println("🔐 [UserDetailsService] Role: " + user.get().getRole());
			return createUserDetailsFromUser(user.get());
		}

		// 2. Chercher dans admins
		Optional<Admin> admin = adminRepository.findByUsername(username);
		if (admin.isPresent()) {

			// 1. Chercher d'abord dans ADMINS
			System.out.println("🟡 [UserDetailsService] Searching in ADMINS table...");

			System.out.println("🟡 [UserDetailsService] Admin found: " + admin.isPresent());

			System.out.println("✅ [UserDetailsService] Found in ADMINS table: " + username);
			System.out.println("🔐 [UserDetailsService] Role: " + admin.get().getRole());

			System.out.println("🔐 [UserDetailsService] Password hash: " + admin.get().getPassword());

			return createUserDetailsFromAdmin(admin.get());
		}

		System.out.println("❌ [UserDetailsService] User not found: " + username);
		throw new UsernameNotFoundException("Utilisateur non trouvé: " + username);
	}

	// Méthode pour créer UserDetails (Spring Security) à partir d'un User (votre
	// entité)
	private UserDetails createUserDetailsFromUser(User user) {
		String role = user.getRole().name(); // "ROLE_USER"
		String simpleRole = role.startsWith("ROLE_") ? role.substring(5) : role; // "USER"

		System.out.println("👤 Creating UserDetails with role: " + simpleRole);

		// Utilise le UserBuilder de Spring Security
		return org.springframework.security.core.userdetails.User.builder().username(user.getUsername())
				.password(user.getPassword()).roles(simpleRole).build();
	}

	// Méthode pour créer UserDetails (Spring Security) à partir d'un Admin (votre
	// entité)
	private UserDetails createUserDetailsFromAdmin(Admin admin) {
		String role = admin.getRole(); // "ROLE_ADMIN"
		String simpleRole = role.startsWith("ROLE_") ? role.substring(5) : role; // "ADMIN"

		System.out.println("👤 Creating Admin UserDetails with role: " + simpleRole);

		// Utilise le UserBuilder de Spring Security
		return org.springframework.security.core.userdetails.User.builder().username(admin.getUsername())
				.password(admin.getPassword()).roles(simpleRole).build();
	}

	// Le reste de votre code reste inchangé
	@Override
	public User createUser(User user) {
		System.out.println("🔵 [UserServiceImpl] Creating user: " + user.getUsername());

		// Vérifier si username ou email existe déjà
		if (userRepository.existsByUsername(user.getUsername())) {
			System.out.println("🔴 [UserServiceImpl] Username already exists: " + user.getUsername());
			throw new IllegalArgumentException("Username already exists");
		}
		if (userRepository.existsByEmail(user.getEmail())) {
			System.out.println("🔴 [UserServiceImpl] Email already exists: " + user.getEmail());
			throw new IllegalArgumentException("Email already exists");
		}

		// Vérifier les dates
		System.out.println("🟡 [UserServiceImpl] User createdAt: " + user.getCreatedAt());
		System.out.println("🟡 [UserServiceImpl] User updatedAt: " + user.getUpdatedAt());

		// S'assurer que les dates sont définies
		if (user.getCreatedAt() == null) {
			user.setCreatedAt(LocalDateTime.now());
			System.out.println("🟡 [UserServiceImpl] Set createdAt to now");
		}
		if (user.getUpdatedAt() == null) {
			user.setUpdatedAt(LocalDateTime.now());
			System.out.println("🟡 [UserServiceImpl] Set updatedAt to now");
		}

		User savedUser = userRepository.save(user);
		System.out.println("🟢 [UserServiceImpl] User created with ID: " + savedUser.getId());

		return savedUser;
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
		User user = new User(username, email, password, Role.ROLE_USER);

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