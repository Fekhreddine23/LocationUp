package com.mobility.mobility_backend.service.auth;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

@Service
public class TwoFactorAuthService {

	private static final String TOTP_ALGORITHM = "HmacSHA1";
	private static final int TIME_STEP = 30; // 30 secondes
	private static final int CODE_DIGITS = 6;

	private final SecureRandom secureRandom;
	private final Map<String, String> userSecrets;

	public TwoFactorAuthService() {
		this.secureRandom = new SecureRandom();
		this.userSecrets = new HashMap<>();
	}

	/**
	 * Génère un secret 2FA compatible Google Authenticator (Base32)
	 */
	public String generateSecret() {
		byte[] buffer = new byte[20]; // 160 bits
		secureRandom.nextBytes(buffer);

		// Convertir en Base32 (compatible Google Authenticator)
		return bytesToBase32(buffer);
	}

	/**
	 * Convertit des bytes en Base32 (sans padding)
	 */
	private String bytesToBase32(byte[] bytes) {
		String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
		StringBuilder result = new StringBuilder();

		int buffer = 0;
		int bitsLeft = 0;

		for (byte b : bytes) {
			buffer = (buffer << 8) | (b & 0xFF);
			bitsLeft += 8;

			while (bitsLeft >= 5) {
				int index = (buffer >> (bitsLeft - 5)) & 0x1F;
				result.append(base32Chars.charAt(index));
				bitsLeft -= 5;
			}
		}

		// Gérer les bits restants
		if (bitsLeft > 0) {
			int index = (buffer << (5 - bitsLeft)) & 0x1F;
			result.append(base32Chars.charAt(index));
		}

		return result.toString();
	}

	/**
	 * Convertit Base32 en bytes
	 */
	private byte[] base32ToBytes(String base32) {
		String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
		base32 = base32.toUpperCase().replaceAll("[^A-Z2-7]", "");

		int buffer = 0;
		int bitsLeft = 0;
		byte[] result = new byte[base32.length() * 5 / 8];
		int resultIndex = 0;

		for (char c : base32.toCharArray()) {
			int value = base32Chars.indexOf(c);
			if (value == -1)
				continue;

			buffer = (buffer << 5) | value;
			bitsLeft += 5;

			if (bitsLeft >= 8) {
				result[resultIndex++] = (byte) (buffer >> (bitsLeft - 8));
				bitsLeft -= 8;
			}
		}

		return result;
	}

	/**
	 * Génère un QR Code URL pour Google Authenticator
	 */
	public String generateQrCodeUrl(String username, String secret) {
		String issuer = "LocationUp";
		// Format OTPAuth URL standard pour Google Authenticator
		return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30", issuer,
				username, secret, issuer);
	}

	/**
	 * Génère un secret et un QR Code pour un utilisateur
	 */
	public Map<String, String> generate2FASecret(String username) {
		try {
			String secret = generateSecret();
			userSecrets.put(username, secret);

			String qrCodeUrl = generateQrCodeUrl(username, secret);

			Map<String, String> result = new HashMap<>();
			result.put("secret", secret);
			result.put("qrCodeUrl", qrCodeUrl);
			result.put("message", "Scannez le QR Code avec Google Authenticator");

			return result;

		} catch (Exception e) {
			throw new RuntimeException("Erreur génération 2FA: " + e.getMessage(), e);
		}
	}

	/**
	 * Génère un code TOTP
	 */
	private String generateTOTP(String secret) {
		try {
			long timeIndex = System.currentTimeMillis() / 1000 / TIME_STEP;

			// Décoder le secret Base32
			byte[] key = base32ToBytes(secret);
			byte[] timeBytes = new byte[8];

			for (int i = 7; i >= 0; i--) {
				timeBytes[i] = (byte) (timeIndex & 0xFF);
				timeIndex >>= 8;
			}

			SecretKeySpec signKey = new SecretKeySpec(key, TOTP_ALGORITHM);
			Mac mac = Mac.getInstance(TOTP_ALGORITHM);
			mac.init(signKey);

			byte[] hash = mac.doFinal(timeBytes);

			int offset = hash[hash.length - 1] & 0xF;
			int binary = ((hash[offset] & 0x7F) << 24) | ((hash[offset + 1] & 0xFF) << 16)
					| ((hash[offset + 2] & 0xFF) << 8) | (hash[offset + 3] & 0xFF);

			int otp = binary % (int) Math.pow(10, CODE_DIGITS);
			return String.format("%0" + CODE_DIGITS + "d", otp);

		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			throw new RuntimeException("Erreur génération TOTP: " + e.getMessage());
		}
	}

	/**
	 * Vérifie un code TOTP avec tolérance de ±1 intervalle de temps
	 */
	public boolean verifyCode(String username, String code) {
		try {
			String secret = userSecrets.get(username);
			if (secret == null || code == null || code.length() != CODE_DIGITS) {
				return false;
			}

			// Vérifier le code actuel et les codes précédent/suivant (tolérance)
			String currentCode = generateTOTP(secret);
			String previousCode = generateTOTPForTimeOffset(secret, -1);
			String nextCode = generateTOTPForTimeOffset(secret, 1);

			return constantTimeEquals(currentCode, code) || constantTimeEquals(previousCode, code)
					|| constantTimeEquals(nextCode, code);

		} catch (Exception e) {
			System.err.println("Erreur vérification code: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Génère un TOTP avec décalage temporel
	 */
	private String generateTOTPForTimeOffset(String secret, int timeOffset) {
		try {
			long timeIndex = (System.currentTimeMillis() / 1000 / TIME_STEP) + timeOffset;

			// Décoder le secret Base32
			byte[] key = base32ToBytes(secret);
			byte[] timeBytes = new byte[8];

			for (int i = 7; i >= 0; i--) {
				timeBytes[i] = (byte) (timeIndex & 0xFF);
				timeIndex >>= 8;
			}

			SecretKeySpec signKey = new SecretKeySpec(key, TOTP_ALGORITHM);
			Mac mac = Mac.getInstance(TOTP_ALGORITHM);
			mac.init(signKey);

			byte[] hash = mac.doFinal(timeBytes);

			int offset = hash[hash.length - 1] & 0xF;
			int binary = ((hash[offset] & 0x7F) << 24) | ((hash[offset + 1] & 0xFF) << 16)
					| ((hash[offset + 2] & 0xFF) << 8) | (hash[offset + 3] & 0xFF);

			int otp = binary % (int) Math.pow(10, CODE_DIGITS);
			return String.format("%0" + CODE_DIGITS + "d", otp);

		} catch (Exception e) {
			return "000000"; // Code par défaut en cas d'erreur
		}
	}

	/**
	 * Comparaison résistante aux attaques par timing
	 */
	private boolean constantTimeEquals(String a, String b) {
		if (a == null || b == null || a.length() != b.length()) {
			return false;
		}

		int result = 0;
		for (int i = 0; i < a.length(); i++) {
			result |= a.charAt(i) ^ b.charAt(i);
		}
		return result == 0;
	}

	/**
	 * Vérifie un code avec un secret fourni
	 */
	public boolean verifyCodeWithSecret(String secret, String code) {
		try {
			if (secret == null || code == null || code.length() != CODE_DIGITS) {
				return false;
			}

			String currentCode = generateTOTP(secret);
			String previousCode = generateTOTPForTimeOffset(secret, -1);
			String nextCode = generateTOTPForTimeOffset(secret, 1);

			return constantTimeEquals(currentCode, code) || constantTimeEquals(previousCode, code)
					|| constantTimeEquals(nextCode, code);

		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Active le 2FA pour un utilisateur
	 */
	public void enable2FA(String username) {
		System.out.println("✅ 2FA activé pour: " + username);
	}

	/**
	 * Désactive le 2FA pour un utilisateur
	 */
	public void disable2FA(String username) {
		userSecrets.remove(username);
		System.out.println("❌ 2FA désactivé pour: " + username);
	}

	/**
	 * Vérifie si le 2FA est configuré pour un utilisateur
	 */
	public boolean is2FAEnabled(String username) {
		return userSecrets.containsKey(username);
	}

	/**
	 * Génère un code de secours
	 */
	public String generateBackupCode() {
		return String.valueOf(100000 + (int) (Math.random() * 900000));
	}
}