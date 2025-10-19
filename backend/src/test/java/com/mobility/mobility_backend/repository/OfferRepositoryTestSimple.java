package com.mobility.mobility_backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@ActiveProfiles("test")
@Transactional // ✅ Utiliser @Transactional
@TestPropertySource(properties = { "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa",
		"spring.datasource.password=", "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=true",
		"spring.jpa.properties.hibernate.format_sql=true" })

public class OfferRepositoryTestSimple {

	@Autowired
	private OfferRepository offerRepository;

	@Test
	void contextLoads() {
		assertThat(offerRepository).isNotNull();
	}

	@Test
	void whenFindAllOnEmptyDatabase_thenReturnEmptyList() {
		// Utilisez count() d'abord pour forcer l'initialisation
		long count = offerRepository.count();
		assertThat(count).isEqualTo(0);

		// Puis findAll()
		var offers = offerRepository.findAll();
		assertThat(offers).isEmpty();
	}
}