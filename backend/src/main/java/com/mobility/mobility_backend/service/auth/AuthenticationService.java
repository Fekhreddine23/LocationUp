package com.mobility.mobility_backend.service.auth;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mobility.mobility_backend.dto.auth.AuthenticationRequest;
import com.mobility.mobility_backend.dto.auth.AuthenticationResponse;
import com.mobility.mobility_backend.dto.auth.RegisterRequest;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.UserRepository;
import com.mobility.mobility_backend.service.JwtService;
import com.mobility.mobility_backend.service.UserService;

import jakarta.transaction.Transactional;

@Service
public class AuthenticationService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository; // ← Ce champ était manquant dans le constructeur

    public AuthenticationService(UserService userService,
                               PasswordEncoder passwordEncoder,
                               JwtService jwtService,
                               AuthenticationManager authenticationManager,
                               UserRepository userRepository) { // ← AJOUTEZ userRepository
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository; // ← Initialisez-le
    }

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        System.out.println("🔵 [AuthService] Registering user: " + request.getUsername());

        // VÉRIFICATION DIRECTE avec repository
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            System.out.println("🔴 [AuthService] Username already exists: " + request.getUsername());
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            System.out.println("🔴 [AuthService] Email already exists: " + request.getEmail());
            throw new RuntimeException("Email already exists");
        }

        // CRÉATION DIRECTE avec toutes les données nécessaires
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        System.out.println("🟡 [AuthService] Saving user directly...");
        User savedUser = userRepository.save(user);
        System.out.println("🟢 [AuthService] User saved with ID: " + savedUser.getId());

        // GÉNÉRATION DU TOKEN
        System.out.println("🟡 [AuthService] Generating JWT token...");
        String jwtToken = jwtService.generateToken(savedUser);
        System.out.println("🟢 [AuthService] JWT Token length: " + (jwtToken != null ? jwtToken.length() : "NULL"));

        // 🔥 CORRECTION ICI : Utiliser le NOUVEAU constructeur avec userId
        AuthenticationResponse response = new AuthenticationResponse(
            jwtToken,
            savedUser.getUsername(),
            savedUser.getRole().name(),
            savedUser.getId()  // ← AJOUTER userId ICI
        );

        System.out.println("✅ [AuthService] Registration SUCCESS - UserId: " + response.getUserId() + ", Token: " + (response.getToken() != null ? "PRESENT" : "MISSING"));

        return response;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        System.out.println("🔐 Attempting authentication for: " + request.getUsername());
        try {
            // Cette ligne doit appeler le UserDetailsService
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            System.out.println("✅ Authentication successful for: " + request.getUsername());

            var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

            var jwtToken = jwtService.generateToken(user);

            // 🔥 CORRECTION ICI : Utiliser le NOUVEAU constructeur avec userId
            return new AuthenticationResponse(
                jwtToken,
                user.getUsername(),
                user.getRole().name(),
                user.getId()  // ← AJOUTER userId ICI
            );

        } catch (Exception e) {
            System.out.println("❌ Authentication failed for " + request.getUsername() + ": " + e.getMessage());
            throw e;
        }
    }
}