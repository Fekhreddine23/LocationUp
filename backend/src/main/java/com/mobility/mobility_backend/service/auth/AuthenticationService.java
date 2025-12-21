package com.mobility.mobility_backend.service.auth;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseCookie;

import com.mobility.mobility_backend.dto.auth.AuthenticationRequest;
import com.mobility.mobility_backend.dto.auth.AuthenticationResponse;
import com.mobility.mobility_backend.dto.auth.RegisterRequest;
import com.mobility.mobility_backend.entity.Admin;
import com.mobility.mobility_backend.entity.Role;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.entity.RefreshToken;
import com.mobility.mobility_backend.repository.AdminRepository;
import com.mobility.mobility_backend.repository.UserRepository;
import com.mobility.mobility_backend.repository.RefreshTokenRepository;
import com.mobility.mobility_backend.service.JwtService;
import com.mobility.mobility_backend.service.UserService;

import jakarta.transaction.Transactional;
import jakarta.servlet.http.Cookie;

@Service
public class AuthenticationService {

	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final AdminRepository adminRepository;
	private final jakarta.servlet.http.HttpServletRequest httpServletRequest;
	private final RefreshTokenRepository refreshTokenRepository;

	public AuthenticationService(UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService,
			AuthenticationManager authenticationManager, UserRepository userRepository,
			AdminRepository adminRepository, jakarta.servlet.http.HttpServletRequest httpServletRequest,
			RefreshTokenRepository refreshTokenRepository) {
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.adminRepository = adminRepository;
		this.httpServletRequest = httpServletRequest;
		this.refreshTokenRepository = refreshTokenRepository;
	}

	@Transactional
	public AuthenticationResponse register(RegisterRequest request) {
		System.out.println("🔵 [AuthService] Registering user: " + request.getUsername());

		// VÉRIFICATION des doublons
		if (userRepository.findByUsername(request.getUsername()).isPresent()) {
			System.out.println("🔴 [AuthService] Username already exists: " + request.getUsername());
			throw new RuntimeException("Username already exists");
		}
		if (userRepository.findByEmail(request.getEmail()).isPresent()) {
			System.out.println("🔴 [AuthService] Email already exists: " + request.getEmail());
			throw new RuntimeException("Email already exists");
		}

		// CRÉATION de l'utilisateur
		User user = new User();
		user.setUsername(request.getUsername());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(request.getRole() != null ? request.getRole() : Role.ROLE_USER); // Rôle par défaut
		user.setCreatedAt(LocalDateTime.now());
		user.setUpdatedAt(LocalDateTime.now());

		System.out.println("🟡 [AuthService] Saving user...");
		User savedUser = userRepository.save(user);
		System.out.println("🟢 [AuthService] User saved with ID: " + savedUser.getId());

		// GÉNÉRATION DU TOKEN
		System.out.println("🟡 [AuthService] Generating JWT token...");
		String jwtToken = jwtService.generateToken(savedUser);
		String refreshToken = generateAndStoreRefreshToken(savedUser.getUsername());

		// RÉPONSE
		AuthenticationResponse response = new AuthenticationResponse(jwtToken, savedUser.getUsername(),
				savedUser.getRole().name(), savedUser.getId());
		response.setRefreshToken(refreshToken);
		response.setAvatarUrl(buildAvatarUrl(savedUser.getAvatarPath()));

		System.out.println("✅ [AuthService] Registration SUCCESS - UserId: " + response.getUserId());
		return response;
	}

	public AuthenticationResponse authenticate(AuthenticationRequest request) {
		System.out.println("🔐 Attempting authentication for: " + request.getUsername());
		try {
			// Authentication
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

			System.out.println("✅ Authentication successful for: " + request.getUsername());

			// ✅ UTILISEZ UserService pour obtenir UserDetails
			UserDetails userDetails = userService.loadUserByUsername(request.getUsername());
			var jwtToken = jwtService.generateToken(userDetails);
			var refreshToken = generateAndStoreRefreshToken(userDetails.getUsername());

			// ✅ POUR RÉCUPÉRER L'ID ET LE RÔLE, FAITES DES REQUÊTES DIRECTES
			Integer userId = null;
			String role = "";
			String avatarUrl = null;

			// Chercher d'abord dans USERS
			Optional<User> userOptional = userRepository.findByUsername(request.getUsername());
			if (userOptional.isPresent()) {
				User user = userOptional.get();
				userId = user.getId();
				role = user.getRole().name();
				avatarUrl = buildAvatarUrl(user.getAvatarPath());
			} else {
				// Si pas trouvé dans USERS, chercher dans ADMINS
				Optional<Admin> adminOptional = adminRepository.findByUsername(request.getUsername());
				if (adminOptional.isPresent()) {
					Admin admin = adminOptional.get();
					userId = admin.getAdminId();
					role = admin.getRole();
					avatarUrl = buildAvatarUrl(admin.getAvatarPath());
				} else {
					throw new RuntimeException(
							"User not found in database after successful authentication: " + request.getUsername());
				}
			}

			AuthenticationResponse response = new AuthenticationResponse(jwtToken, request.getUsername(), role, userId);
			response.setAvatarUrl(avatarUrl);
			response.setRefreshToken(refreshToken);
			return response;

		} catch (Exception e) {
			System.out.println("❌ Authentication failed for " + request.getUsername() + ": " + e.getMessage());
			throw e;
		}
	}

	private String buildAvatarUrl(String avatarPath) {
		if (avatarPath == null || avatarPath.isBlank()) {
			return null;
		}
		return "/api/users/avatar/" + avatarPath;
	}

	public AuthenticationResponse refreshAccessToken() {
		String refreshToken = extractRefreshTokenFromCookie();
		if (refreshToken == null) {
			throw new RuntimeException("Refresh token manquant");
		}

		RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
				.orElseThrow(() -> new RuntimeException("Refresh token invalide"));
		if (stored.isRevoked() || stored.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
			throw new RuntimeException("Refresh token expiré ou révoqué");
		}

		String username = stored.getUsername();
		UserDetails userDetails = userService.loadUserByUsername(username);

		String newAccessToken = jwtService.generateToken(userDetails);
		String newRefreshToken = rotateRefreshToken(stored);

		Integer userId = null;
		String role = "";
		String avatarUrl = null;
		Optional<User> userOptional = userRepository.findByUsername(username);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			userId = user.getId();
			role = user.getRole().name();
			avatarUrl = buildAvatarUrl(user.getAvatarPath());
		} else {
			Optional<Admin> adminOptional = adminRepository.findByUsername(username);
			if (adminOptional.isPresent()) {
				Admin admin = adminOptional.get();
				userId = admin.getAdminId();
				role = admin.getRole();
				avatarUrl = buildAvatarUrl(admin.getAvatarPath());
			} else {
				throw new RuntimeException("Utilisateur introuvable");
			}
		}

		AuthenticationResponse response = new AuthenticationResponse(newAccessToken, username, role, userId);
		response.setAvatarUrl(avatarUrl);
		response.setRefreshToken(newRefreshToken);
		return response;
	}

	private String extractRefreshTokenFromCookie() {
		if (httpServletRequest.getCookies() == null) {
			return null;
		}
		for (Cookie cookie : httpServletRequest.getCookies()) {
			if ("refresh_token".equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}

	public ResponseCookie buildRefreshCookie(String refreshToken) {
		return ResponseCookie.from("refresh_token", refreshToken != null ? refreshToken : "")
				.httpOnly(true)
				.secure(true)
				.sameSite("Lax")
				.path("/")
				.maxAge(jwtService.getRefreshExpirationMs() / 1000)
				.build();
	}

	private String generateAndStoreRefreshToken(String username) {
		String token = java.util.UUID.randomUUID().toString();
		RefreshToken rt = new RefreshToken();
		rt.setToken(token);
		rt.setUsername(username);
		rt.setExpiresAt(java.time.LocalDateTime.now().plus(java.time.Duration.ofMillis(jwtService.getRefreshExpirationMs())));
		rt.setRevoked(false);
		rt.setCreatedAt(java.time.LocalDateTime.now());
		rt.setUpdatedAt(java.time.LocalDateTime.now());
		refreshTokenRepository.save(rt);
		return token;
	}

	private String rotateRefreshToken(RefreshToken existing) {
		existing.setRevoked(true);
		existing.setUpdatedAt(java.time.LocalDateTime.now());
		refreshTokenRepository.save(existing);
		return generateAndStoreRefreshToken(existing.getUsername());
	}
}
