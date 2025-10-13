package com.mobility.mobility_backend.repository;

import com.mobility.mobility_backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("ci")
@Transactional  // ✅ Utiliser @Transactional au lieu de cleanDatabase()
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindUser() {
        // Given
        String uniqueUsername = "testuser_" + System.currentTimeMillis();
        String uniqueEmail = "test_" + System.currentTimeMillis() + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "password123");
        
        // When
        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        
        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(uniqueUsername);
        assertThat(foundUser.get().getEmail()).isEqualTo(uniqueEmail);
    }

    @Test
    void testFindByUsername() {
        // Given
        String uniqueUsername = "finduser_" + System.currentTimeMillis();
        String uniqueEmail = "find_" + System.currentTimeMillis() + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "password123");
        userRepository.save(user);
        
        // When
        Optional<User> foundUser = userRepository.findByUsername(uniqueUsername);
        
        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(uniqueUsername);
    }

    @Test
    void testFindByEmail() {
        // Given
        String uniqueUsername = "emailuser_" + System.currentTimeMillis();
        String uniqueEmail = "email_" + System.currentTimeMillis() + "@example.com";
        User user = new User(uniqueUsername, uniqueEmail, "password123");
        userRepository.save(user);
        
        // When
        Optional<User> foundUser = userRepository.findByEmail(uniqueEmail);
        
        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(uniqueEmail);
    }
}
