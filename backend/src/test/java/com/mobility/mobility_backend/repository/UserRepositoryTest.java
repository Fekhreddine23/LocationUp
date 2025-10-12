package com.mobility.mobility_backend.repository;

import com.mobility.mobility_backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("ci")  // ✅ AJOUTÉ
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveAndFindUser() {
        // Given
        User user = new User("john_doe", "john@example.com", "password123");
        
        // When
        User savedUser = userRepository.save(user);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        
        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("john_doe");
        assertThat(foundUser.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    public void testFindByUsername() {
        // Given
        User user = new User("jane_smith", "jane@example.com", "password123");
        userRepository.save(user);
        
        // When
        Optional<User> foundUser = userRepository.findByUsername("jane_smith");
        
        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("jane@example.com");
    }

    @Test
    public void testFindByEmail() {
        // Given
        User user = new User("bob_wilson", "bob@example.com", "password123");
        userRepository.save(user);
        
        // When
        Optional<User> foundUser = userRepository.findByEmail("bob@example.com");
        
        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("bob_wilson");
    }
}