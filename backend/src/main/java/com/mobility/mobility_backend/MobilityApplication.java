package com.mobility.mobility_backend; // Assure-toi que ton package racine est bien défini ici

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan("com.mobility.mobility_backend") // Assure-toi de scanner tout le package et ses sous-packages
public class MobilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(MobilityApplication.class, args);
	}

	/*
	 * @Bean public CommandLineRunner dataInitializer(UserRepository userRepository,
	 * PasswordEncoder passwordEncoder) { return args -> {
	 * System.out.println("🎯🎯🎯 BEAN DATAINITIALIZER EXECUTING! 🎯🎯🎯");
	 *
	 * // Nettoyer la base de données avant de créer un nouvel utilisateur
	 * userRepository.deleteAll();
	 *
	 * // Créer un utilisateur User user = new User(); user.setUsername("testuser");
	 * user.setEmail("test@example.com");
	 * user.setPassword(passwordEncoder.encode("password123")); // Hachage du mot de
	 * passe user.setRole(Role.ROLE_USER); // Assigner un rôle à l'utilisateur
	 *
	 * // Sauvegarder l'utilisateur dans la base de données User savedUser =
	 * userRepository.save(user); System.out.println("✅ User created with ID: " +
	 * savedUser.getId()); System.out.println("🔐 Password hash: " +
	 * savedUser.getPassword()); }; }
	 */
}
