package com.mobility.mobility_backend.service.auth;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mobility.mobility_backend.dto.auth.AuthenticationRequest;
import com.mobility.mobility_backend.dto.auth.AuthenticationResponse;
import com.mobility.mobility_backend.dto.auth.RegisterRequest;
import com.mobility.mobility_backend.entity.Admin;
import com.mobility.mobility_backend.entity.Role;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.AdminRepository;
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
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public AuthenticationService(UserService userService,
                               PasswordEncoder passwordEncoder,
                               JwtService jwtService,
                               AuthenticationManager authenticationManager,
                               UserRepository userRepository,
                               AdminRepository adminRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }

    // ⭐ AJOUTEZ CETTE MÉTHODE MANQUANTE
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

        // RÉPONSE
        AuthenticationResponse response = new AuthenticationResponse(
            jwtToken,
            savedUser.getUsername(),
            savedUser.getRole().name(),
            savedUser.getId()
        );

        System.out.println("✅ [AuthService] Registration SUCCESS - UserId: " + response.getUserId());
        return response;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        System.out.println("🔐 Attempting authentication for: " + request.getUsername());
        try {
            // Authentication
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            System.out.println("✅ Authentication successful for: " + request.getUsername());

            // Chercher d'abord dans USERS
            Optional<User> userOptional = userRepository.findByUsername(request.getUsername());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                var jwtToken = jwtService.generateToken(user);

                return new AuthenticationResponse(
                    jwtToken,
                    user.getUsername(),
                    user.getRole().name(),
                    user.getId()
                );
            }

            // Si pas trouvé dans USERS, chercher dans ADMINS
            Optional<Admin> adminOptional = adminRepository.findByUsername(request.getUsername());
            if (adminOptional.isPresent()) {
                Admin admin = adminOptional.get();
                var jwtToken = jwtService.generateToken(admin.getUsername());

                return new AuthenticationResponse(
                    jwtToken,
                    admin.getUsername(),
                    admin.getRole(),
                    admin.getAdminId()
                );
            }

            throw new RuntimeException("User not found in database after successful authentication: " + request.getUsername());

        } catch (Exception e) {
            System.out.println("❌ Authentication failed for " + request.getUsername() + ": " + e.getMessage());
            throw e;
        }
    }
}