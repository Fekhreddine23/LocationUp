package com.mobility.mobility_backend.service;

import java.util.List;
import java.util.Optional;

import com.mobility.mobility_backend.dto.UserDTO;

public interface UserService {
	
	 UserDTO createUser(String username, String email, String password);
	    
	    Optional<UserDTO> getUserById(Long id);
	    
	    Optional<UserDTO> getUserByUsername(String username);
	    
	    Optional<UserDTO> getUserByEmail(String email);
	    
	    List<UserDTO> getAllUsers();
	    
	    boolean userExists(String username);
	    
	    boolean emailExists(String email);
	    
	    boolean deleteUser(Long id);

}
