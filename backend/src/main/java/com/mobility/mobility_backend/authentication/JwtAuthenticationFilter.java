package com.mobility.mobility_backend.authentication;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mobility.mobility_backend.service.JwtService;
import com.mobility.mobility_backend.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@ConditionalOnProperty(prefix = "spring.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserService userService;

	@Autowired
	public JwtAuthenticationFilter(JwtService jwtService, UserService userService) {
		this.jwtService = jwtService;
		this.userService = userService;
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain) throws ServletException, IOException {

		final String path = request.getServletPath();

		// 🔥 CRITIQUE : LISTE DES PATHS À IGNORER COMPLÈTEMENT
		if (shouldSkipJwtFilter(path)) {
			System.out.println("🔓 [JwtAuthFilter] SKIPPING JWT for public path: " + path);
			filterChain.doFilter(request, response);
			return;
		}

		System.out.println("=== 🔐 [JwtAuthFilter] START ===");
		System.out.println("🔐 Request: " + request.getMethod() + " " + path);

		final String authHeader = request.getHeader("Authorization");
		System.out.println("🔐 Authorization Header present: " + (authHeader != null));

		// Vérifier si c'est une requête OPTIONS (preflight CORS)
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			System.out.println("🔐 OPTIONS request - skipping JWT filter");
			filterChain.doFilter(request, response);
			return;
		}

		String jwt = null;
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			jwt = authHeader.substring(7);
		} else {
			String tokenParam = request.getParameter("access_token");
			if (tokenParam != null && !tokenParam.isBlank()) {
				jwt = tokenParam;
				System.out.println("🔐 Using access_token query parameter");
			}
		}

		if (jwt == null || jwt.isBlank()) {
			System.out.println("❌ No Bearer token found or invalid format");
			System.out.println("=== 🔐 [JwtAuthFilter] END (No Token) ===");

			// ⚠️ IMPORTANT : Pour les routes API protégées, on bloque si pas de token
			if (isProtectedApiPath(path)) {
				System.err.println("🚫 Access denied to protected path without token: " + path);
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("{\"error\": \"Authentication required\"}");
				return;
			}

			filterChain.doFilter(request, response);
			return;
		}

		try {
			System.out.println("🔐 JWT Token length: " + jwt.length());

			String userEmail = jwtService.extractUsername(jwt);
			System.out.println("🔐 Extracted username/email: " + userEmail);

			if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				System.out.println("🔐 Loading user details for: " + userEmail);

				UserDetails userDetails;
				try {
					userDetails = this.userService.loadUserByUsername(userEmail);
					System.out.println("🔐 User found: " + userDetails.getUsername());
					System.out.println("🔐 User authorities: " + userDetails.getAuthorities());
				} catch (UsernameNotFoundException e) {
					System.err.println("❌ User not found: " + userEmail);
					System.out.println("=== 🔐 [JwtAuthFilter] END (User Not Found) ===");
					filterChain.doFilter(request, response);
					return;
				}

				System.out.println("🔐 Validating token...");
				boolean isTokenValid = jwtService.isTokenValid(jwt, userDetails);
				System.out.println("🔐 Token valid: " + isTokenValid);

				if (isTokenValid) {
					UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
							null, userDetails.getAuthorities());
					authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authToken);
					System.out.println("✅ Authentication set for user: " + userEmail);
				} else {
					System.err.println("❌ Token validation failed for user: " + userEmail);
				}
			} else {
				if (userEmail == null) {
					System.err.println("❌ Could not extract username from token");
				} else {
					System.out.println("ℹ️ Authentication already set for user: " + userEmail);
				}
			}

		} catch (Exception e) {
			System.err.println("❌ ERROR in JWT filter: " + e.getMessage());
			e.printStackTrace();
		}

		System.out.println("=== 🔐 [JwtAuthFilter] END ===");
		filterChain.doFilter(request, response);
	}

	/**
	 * 🔥 LISTE DES PATHS PUBLICS - DOIT MATCHER AVEC SECURITYCONFIG
	 */
	private boolean shouldSkipJwtFilter(String path) {
		return path.startsWith("/api/debug/") || path.startsWith("/api/auth/")
				|| path.startsWith("/swagger-ui/") || path.startsWith("/v3/api-docs/")
				|| path.startsWith("/h2-console/") || path.startsWith("/actuator/")
				|| path.equals("/api/offers")
				|| (path.startsWith("/api/offers/") && !path.contains("/favorite"))
				|| path.startsWith("/api/users/avatar/")
				|| path.startsWith("/api/payments/webhook")
				|| path.startsWith("/api/identity/webhook")
				|| path.startsWith("/api/reservations/test-public") || path.startsWith("/api/reservations/test-simple")
				|| path.startsWith("/api/test/");
	}

	/**
	 * Vérifie si le path fait partie des routes API protégées
	 */
	private boolean isProtectedApiPath(String path) {
		return path.startsWith("/api/admin/") || path.startsWith("/api/reservations/") || path.startsWith("/api/users/")
				|| (path.startsWith("/api/") && !shouldSkipJwtFilter(path));
	}
}
