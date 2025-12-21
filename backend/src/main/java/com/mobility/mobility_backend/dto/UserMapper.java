package com.mobility.mobility_backend.dto;

import org.springframework.stereotype.Component;

import com.mobility.mobility_backend.entity.User;

@Component
public class UserMapper {

	public static UserDTO toDTO(User user) {
		if (user == null) {
			return null;
		}
		UserDTO dto = new UserDTO();
		dto.setId(user.getId());
		dto.setUsername(user.getUsername());
		dto.setEmail(user.getEmail());
		dto.setFirstname(user.getFirstName());
		dto.setLastname(user.getLastName());
		dto.setRole(user.getRole() != null ? user.getRole().name() : null);
		if (user.getAvatarPath() != null && !user.getAvatarPath().isBlank()) {
			dto.setAvatarUrl("/api/users/avatar/" + user.getAvatarPath());
		}
		return dto;
	}

	public static User toEntity(UserDTO userDTO) {
		if (userDTO == null) {
			return null;
		}
		User user = new User(userDTO.getUsername(), userDTO.getEmail(), "", null);
		user.setId(userDTO.getId());
		return user;
	}

	public static User toEntityWithPassword(UserDTO userDTO, String password) {
		if (userDTO == null) {
			return null;
		}
		User user = new User(userDTO.getUsername(), userDTO.getEmail(), password, null);
		user.setId(userDTO.getId());
		return user;
	}

}
