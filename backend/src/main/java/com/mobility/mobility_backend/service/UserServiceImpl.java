package com.mobility.mobility_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mobility.mobility_backend.dto.UserDTO;
import com.mobility.mobility_backend.dto.UserMapper;
import com.mobility.mobility_backend.entity.Role;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	// Implémentation de loadUserByUsername de UserDetailsService
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
	}

	@Override
	public User createUser(User user) {
		// Vérifier si username ou email existe déjà
		if (userRepository.existsByUsername(user.getUsername())) {
			throw new IllegalArgumentException("Username already exists");
		}
		if (userRepository.existsByEmail(user.getEmail())) {
			throw new IllegalArgumentException("Email already exists");
		}
		return userRepository.save(user);
	}

	@Override
	public User updateUser(Integer id, User user) {
		return userRepository.findById(id).map(existingUser -> {
			existingUser.setUsername(user.getUsername());
			existingUser.setEmail(user.getEmail());
			existingUser.setRole(user.getRole());
			// Ne pas mettre à jour le password ici directement
			return userRepository.save(existingUser);
		}).orElseThrow(() -> new IllegalArgumentException("User not found"));
	}

	@Override
	public Optional<UserDTO> getUserById(Integer id) {
		return userRepository.findById(id).map(UserMapper::toDTO);
	}

	@Override
	public Optional<UserDTO> getUserByUsername(String username) {
		return userRepository.findByUsername(username).map(UserMapper::toDTO);
	}

	@Override
	public Optional<UserDTO> getUserByEmail(String email) {
		return userRepository.findByEmail(email).map(UserMapper::toDTO);
	}

	@Override
	public List<UserDTO> getAllUsers() {
		return userRepository.findAll().stream().map(UserMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	public boolean userExists(String username) {
		return userRepository.existsByUsername(username);
	}

	@Override
	public boolean emailExists(String email) {
		return userRepository.existsByEmail(email);
	}

	@Override
	public boolean deleteUser(Integer id) {
		if (userRepository.existsById(id)) {
			userRepository.deleteById(id);
			return true;
		}
		return false;
	}

	@Override
	public UserDTO createUser(String username, String email, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User createUser(String username, String email, String password, Role role) {
		// TODO Auto-generated method stub
		return null;
	}

}
