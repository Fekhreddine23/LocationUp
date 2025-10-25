package com.mobility.mobility_backend.controller;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.UserRepository;
import com.mobility.mobility_backend.service.JwtService;

@RestController
@RequestMapping("/api/debug")
public class DebugAuthController {


    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;


    private final JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    public DebugAuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/test-password")
    public ResponseEntity<?> testPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        System.out.println("=== DEBUG AUTH ===");
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("User found: " + user.getUsername());
        System.out.println("Stored password: " + user.getPassword());
        System.out.println("Password length: " + user.getPassword().length());
        System.out.println("Starts with $2a$: " + user.getPassword().startsWith("$2a$"));

        boolean matches = passwordEncoder.matches(password, user.getPassword());
        System.out.println("Password matches: " + matches);

        if (matches) {
            String token = jwtService.generateToken(user);
            return ResponseEntity.ok(Map.of(
                "token", token,
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "passwordMatch", true
            ));
        } else {
            return ResponseEntity.status(403).body(Map.of(
                "error", "Password mismatch",
                "passwordMatch", false
            ));
        }
    }





    @PostMapping("/verify-auth")
    public String verifyAuthentication(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        try {
            // 1. Vérifier si l'utilisateur existe en base
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found in database"));

            System.out.println("🔍 User found: " + user.getUsername());
            System.out.println("🔐 Stored hash: " + user.getPassword());

            // 2. Vérifier le mot de passe
            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            System.out.println("✅ Password matches: " + passwordMatches);

            // 3. Vérifier UserDetailsService
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                System.out.println("👤 UserDetails loaded: " + userDetails.getUsername());
                System.out.println("🔑 UserDetails password: " + userDetails.getPassword());
                System.out.println("🎭 UserDetails authorities: " + userDetails.getAuthorities());
            } catch (Exception e) {
                System.out.println("❌ UserDetailsService error: " + e.getMessage());
            }

            return "User: " + username +
                   "\nPassword matches: " + passwordMatches +
                   "\nStored hash: " + user.getPassword();

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}