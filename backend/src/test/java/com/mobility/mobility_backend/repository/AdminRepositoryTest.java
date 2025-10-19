package com.mobility.mobility_backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.mobility.mobility_backend.entity.Admin;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdminRepositoryTest {

	@Autowired
	private AdminRepository adminRepository;

	/*
	 * Qu'un admin peut être sauvegardé avec adminRepository.save()
	 *
	 * Que l'admin sauvegardé a bien un ID généré (non null)
	 *
	 * Que les données sont correctement persistées (username = "testadmin")
	 */
	@Test
	void whenSaveAdmin_thenAdminIsSaved() {
		// Given
		Admin admin = createTestAdmin();

		// When
		Admin savedAdmin = adminRepository.save(admin);

		// Then
		assertThat(savedAdmin).isNotNull();
		assertThat(savedAdmin.getAdminId()).isNotNull();
		assertThat(savedAdmin.getUsername()).isEqualTo("testadmin");
	}

	/*
	 * Que findByUsername() retourne bien l'admin correspondant
	 *
	 * Que l'admin trouvé a le bon username ("testadmin")
	 *
	 * Que la méthode retourne un Optional contenant l'admin
	 */
	@Test
	void whenFindByUsername_thenReturnAdmin() {
		// Given
		Admin admin = createTestAdmin();
		adminRepository.save(admin);

		// When
		Optional<Admin> foundAdmin = adminRepository.findByUsername("testadmin");

		// Then
		assertThat(foundAdmin).isPresent();
		assertThat(foundAdmin.get().getUsername()).isEqualTo("testadmin");
	}

	/*
	 * Que findByEmail() retourne bien l'admin correspondant
	 *
	 * Que l'admin trouvé a le bon email ("admin@test.com")
	 *
	 * Que la recherche par email fonctionne correctement
	 */
	@Test
	void whenFindByEmail_thenReturnAdmin() {
		// Given
		Admin admin = createTestAdmin();
		adminRepository.save(admin);

		// When
		Optional<Admin> foundAdmin = adminRepository.findByEmail("admin@test.com");

		// Then
		assertThat(foundAdmin).isPresent();
		assertThat(foundAdmin.get().getEmail()).isEqualTo("admin@test.com");
	}

	/*
	 * Que existsByUsername() retourne true quand l'username existe
	 *
	 * Que la méthode détecte correctement la présence d'un username
	 */
	@Test
	void whenExistsByUsername_thenReturnTrue() {
		// Given
		Admin admin = createTestAdmin();
		adminRepository.save(admin);

		// When
		boolean exists = adminRepository.existsByUsername("testadmin");

		// Then
		assertThat(exists).isTrue();
	}

	/*
	 * Que existsByEmail() retourne true quand l'email existe
	 *
	 * Que la méthode détecte correctement la présence d'un email
	 */
	@Test
	void whenExistsByEmail_thenReturnTrue() {
		// Given
		Admin admin = createTestAdmin();
		adminRepository.save(admin);

		// When
		boolean exists = adminRepository.existsByEmail("admin@test.com");

		// Then
		assertThat(exists).isTrue();
	}

	// Créer un admin de test avec des données cohérentes
	private Admin createTestAdmin() {
		Admin admin = new Admin();
		admin.setUsername("testadmin");
		admin.setEmail("admin@test.com");
		admin.setPassword("password123");
		admin.setAdminLevel(1);
		return admin;
	}
}