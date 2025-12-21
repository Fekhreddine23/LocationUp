package com.mobility.mobility_backend.util;

public final class SanitizationUtils {
	private static final int DEFAULT_MAX_LENGTH = 1000;

	private SanitizationUtils() {
	}

	/**
	 * Nettoie une chaîne pour éviter l'injection HTML/JS : supprime balises
	 * évidentes, caractères de contrôle et tronque à une longueur raisonnable.
	 */
	public static String sanitizeText(String input) {
		if (input == null) {
			return null;
		}
		String cleaned = input.replaceAll("[<>]", " ").replaceAll("[\\p{Cntrl}]", "").trim();
		if (cleaned.length() > DEFAULT_MAX_LENGTH) {
			cleaned = cleaned.substring(0, DEFAULT_MAX_LENGTH);
		}
		return cleaned;
	}
}
