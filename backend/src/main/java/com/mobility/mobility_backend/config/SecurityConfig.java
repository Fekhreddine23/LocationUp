package com.mobility.mobility_backend.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.mobility.mobility_backend.authentication.JwtAuthenticationFilter;

@Profile({ "default", "dev", "!test" })
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthFilter;
	private final UserDetailsService userDetailsService;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
		this.jwtAuthFilter = jwtAuthFilter;
		this.userDetailsService = userDetailsService;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		System.out.println("🔒 [DEBUG] SecurityConfig is being loaded!");

		http.csrf(csrf -> csrf.disable()).cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.headers(headers -> headers.frameOptions(frame -> frame.disable()).contentSecurityPolicy(
						csp -> csp.policyDirectives("frame-ancestors 'self' http://localhost:8088")))
				.authorizeHttpRequests(auth -> auth
						// Routes publiques - DOIVENT ÊTRE EN PREMIER
						.requestMatchers("/api/notifications/**").permitAll().requestMatchers("/api/debug/**")
						.permitAll().requestMatchers("/ws/**").permitAll() // Pour WebSocket si utilisé plus tard
						.requestMatchers("/api/auth/**").permitAll()
						.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
						.requestMatchers("/h2-console/**").permitAll().requestMatchers("/actuator/**").permitAll()

						.requestMatchers("/api/reservations/test-public").permitAll()
						.requestMatchers("/api/reservations/test-simple").permitAll().requestMatchers("/api/test/**")
						.permitAll().requestMatchers("/api/debug/**").permitAll()

						// ✅ AJOUTEZ CETTE LIGNE - OFFRES ACCESSIBLES SANS AUTH
						.requestMatchers("/api/offers/**").permitAll()

						.requestMatchers(HttpMethod.POST, "/api/reservations").authenticated()

						// Routes protégées par rôle
						.requestMatchers("/api/admin/**").hasRole("ADMIN")

						// Routes nécessitant une authentification
						.requestMatchers("/api/reservations/**").authenticated().requestMatchers("/api/users/**")
						.authenticated()

						// Toutes les autres routes API
						.requestMatchers("/api/**").authenticated()

						// Toutes les autres routes
						.anyRequest().permitAll())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authenticationProvider(authenticationProvider())
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		System.out.println("✅ [DEBUG] SecurityConfig loaded successfully!");

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(
				Arrays.asList("http://localhost:4200", "http://127.0.0.1:4200", "http://localhost:3000"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
		configuration.setAllowedHeaders(
				Arrays.asList("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With",
						"Access-Control-Request-Method", "Access-Control-Request-Headers", "Cache-Control"));
		configuration.setExposedHeaders(
				Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "Authorization"));
		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

 
	@Bean
	public PasswordEncoder passwordEncoder() {
	    System.out.println("🔐 [SecurityConfig] Creating BCryptPasswordEncoder");
	    return new BCryptPasswordEncoder();
	}

		// Pour revenir à BCrypt plus tard :
		// return new BCryptPasswordEncoder();
	}
