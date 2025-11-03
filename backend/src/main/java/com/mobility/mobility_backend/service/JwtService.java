package com.mobility.mobility_backend.service;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	@Value("${app.jwt.secret:mySecretKey12345678901234567890123456789012}") // 32 caractères minimum
	private String secretKey;

	@Value("${app.jwt.expiration:86400000}") // 24 heures par défaut
	private long jwtExpiration;

	public String generateToken(UserDetails userDetails) {

		Map<String, Object> claims = new HashMap<>();

		// Ajouter le rôle dans les claims
	    String role = userDetails.getAuthorities().stream()
	            .findFirst()
	            .map(GrantedAuthority::getAuthority)
	            .orElse("ROLE_USER");

	    claims.put("role", role);
	    System.out.println("🔐 Adding role to JWT: " + role);
		return generateToken(claims, userDetails);
	}

	public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
		// Version simplifiée sans JJWT
		String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
		String payload = createPayload(userDetails.getUsername(), extraClaims);

		String headerEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes());
		String payloadEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes());

		String signature = createSignature(headerEncoded + "." + payloadEncoded);

		return headerEncoded + "." + payloadEncoded + "." + signature;
	}

	// ⭐ NOUVELLE MÉTHODE : Pour générer un token avec juste un username
	public String generateToken(String username) {
		return generateToken(username, new HashMap<>());
	}

	// ⭐ NOUVELLE MÉTHODE : Pour générer un token avec username et claims
	public String generateToken(String username, Map<String, Object> extraClaims) {
		String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
		String payload = createPayload(username, extraClaims);

		String headerEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(header.getBytes());
		String payloadEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes());

		String signature = createSignature(headerEncoded + "." + payloadEncoded);

		return headerEncoded + "." + payloadEncoded + "." + signature;
	}

	private String createPayload(String username, Map<String, Object> extraClaims) {
		long currentTime = System.currentTimeMillis();
		long expirationTime = currentTime + jwtExpiration;

		StringBuilder payload = new StringBuilder();
		payload.append("{\"sub\":\"").append(username).append("\"");
		payload.append(",\"iat\":").append(currentTime / 1000);
		payload.append(",\"exp\":").append(expirationTime / 1000);

		for (Map.Entry<String, Object> entry : extraClaims.entrySet()) {
			payload.append(",\"").append(entry.getKey()).append("\":");
			if (entry.getValue() instanceof String) {
				payload.append("\"").append(entry.getValue()).append("\"");
			} else {
				payload.append(entry.getValue());
			}
		}

		payload.append("}");
		return payload.toString();
	}

	private String createSignature(String data) {
		try {
			javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
			Key key = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
			mac.init(key);
			byte[] signatureBytes = mac.doFinal(data.getBytes());
			return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
		} catch (Exception e) {
			throw new RuntimeException("Error creating signature", e);
		}
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		if (token == null || !token.contains(".")) {
			return false;
		}

		final String username = extractUsername(token);
		return (username != null && username.equals(userDetails.getUsername())) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		try {
			Date expiration = extractExpiration(token);
			return expiration.before(new Date());
		} catch (Exception e) {
			return true;
		}
	}

	public String extractUsername(String token) {
		return extractClaim(token, "sub");
	}

	private Date extractExpiration(String token) {
		try {
			String exp = extractClaim(token, "exp");
			if (exp != null) {
				long expirationSeconds = Long.parseLong(exp);
				return new Date(expirationSeconds * 1000);
			}
		} catch (Exception e) {
			// Ignorer l'erreur
		}
		return new Date(0); // Date dans le passé
	}

	private String extractClaim(String token, String claimName) {
		try {
			String[] parts = token.split("\\.");
			if (parts.length != 3) {
				return null;
			}

			String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
			// Parse JSON simple pour extraire le claim
			String search = "\"" + claimName + "\":";
			int startIndex = payload.indexOf(search);
			if (startIndex == -1) {
				return null;
			}

			startIndex += search.length();
			int endIndex = payload.indexOf(",", startIndex);
			if (endIndex == -1) {
				endIndex = payload.indexOf("}", startIndex);
			}

			if (endIndex == -1) {
				return null;
			}

			String value = payload.substring(startIndex, endIndex).trim();
			if (value.startsWith("\"") && value.endsWith("\"")) {
				value = value.substring(1, value.length() - 1);
			}

			return value;
		} catch (Exception e) {
			return null;
		}
	}
}