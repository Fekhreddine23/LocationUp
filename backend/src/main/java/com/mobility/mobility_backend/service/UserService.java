package com.mobility.mobility_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.mobility.mobility_backend.dto.UserDTO;
import com.mobility.mobility_backend.entity.Role;
import com.mobility.mobility_backend.entity.User;

public interface UserService extends UserDetailsService {

	// UserDTO createUser(String username, String email, String password);

	Optional<UserDTO> getUserById(Integer id);

	Optional<UserDTO> getUserByUsername(String username);

	Optional<UserDTO> getUserByEmail(String email);

	List<UserDTO> getAllUsers();

	boolean userExists(String username);

	boolean emailExists(String email);

	User updateUser(Integer id, User user);

	boolean deleteUser(Integer id);

	User createUser(User user);

	UserDTO createUser(String username, String email, String password);

	// Pour créer à partir de champs simples (optionnel)
	User createUser(String username, String email, String password, Role role);



}
