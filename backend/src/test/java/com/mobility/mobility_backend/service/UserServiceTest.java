package com.mobility.mobility_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import com.mobility.mobility_backend.dto.UserDTO;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("ci")
public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserServiceImpl userService;

	@Test
	public void testCreateUser() {
		// Given
		String username = "john_doe";
		String email = "john@example.com";
		String password = "password123";

		User savedUser = new User(username, email, password, null);
		savedUser.setId(1);

		when(userRepository.existsByUsername(username)).thenReturn(false);
		when(userRepository.existsByEmail(email)).thenReturn(false);
		when(userRepository.save(any(User.class))).thenReturn(savedUser);

		// When
		UserDTO result = userService.createUser(username, email, password);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getUsername()).isEqualTo(username);
		assertThat(result.getEmail()).isEqualTo(email);

		verify(userRepository).existsByUsername(username);
		verify(userRepository).existsByEmail(email);
		verify(userRepository).save(any(User.class));
	}

	@Test
	public void testGetUserById() {
		// Given
		Integer userId = 1;
		User user = new User("john_doe", "john@example.com", "password123", null);
		user.setId(userId);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// When
		Optional<UserDTO> result = userService.getUserById(userId);

		// Then
		assertThat(result).isPresent();
		assertThat(result.get().getId()).isEqualTo(userId);
		assertThat(result.get().getUsername()).isEqualTo("john_doe");
	}
}
