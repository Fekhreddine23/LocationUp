package com.mobility.mobility_backend.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${app.jwt.secret:mySecretKey}")
    private String secretKey;

    @Value("${app.jwt.expiration:86400000}") // 24 heures par défaut
    private long jwtExpiration;

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        // Simulation de génération de token
        String username = userDetails.getUsername();
        long expirationTime = System.currentTimeMillis() + jwtExpiration;
        
        // Création d'un token basique (à remplacer par une vraie implémentation JWT plus tard)
        String tokenContent = username + "|" + expirationTime + "|" + userDetails.getAuthorities();
        String encodedToken = Base64.getEncoder().encodeToString(tokenContent.getBytes());
        
        return "simulated-token-" + encodedToken;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (token == null || !token.startsWith("simulated-token-")) {
            return false;
        }
        
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        try {
            String encodedPart = token.replace("simulated-token-", "");
            String decoded = new String(Base64.getDecoder().decode(encodedPart));
            String[] parts = decoded.split("\\|");
            long expirationTime = Long.parseLong(parts[1]);
            return expirationTime < System.currentTimeMillis();
        } catch (Exception e) {
            return true; // Si erreur de parsing, considérer comme expiré
        }
    }

    public String extractUsername(String token) {
        try {
            String encodedPart = token.replace("simulated-token-", "");
            String decoded = new String(Base64.getDecoder().decode(encodedPart));
            String[] parts = decoded.split("\\|");
            return parts[0];
        } catch (Exception e) {
            return null;
        }
    }
}