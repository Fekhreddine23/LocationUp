package com.mobility.mobility_backend.service.auth;

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
        // Créer l'utilisateur
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        // Sauvegarder l'utilisateur
        User savedUser = userService.createUser(user);

        // Générer le token JWT
        String jwtToken = jwtService.generateToken(savedUser);

        // Retourner la réponse
        return new AuthenticationResponse(jwtToken, savedUser.getUsername(), savedUser.getRole().name());
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

            return new AuthenticationResponse(jwtToken, user.getUsername(), user.getRole().name());


        } catch (Exception e) {
            System.out.println("❌ Authentication failed for " + request.getUsername() + ": " + e.getMessage());
            throw e;
        }
    }
}