package com.mobility.mobility_backend.dto;

import com.mobility.mobility_backend.entity.User;

public class UserMapper {
	
	public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail());
    }
    
    public static User toEntity(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        User user = new User(userDTO.getUsername(), userDTO.getEmail(), "");
        user.setId(userDTO.getId());
        return user;
    }
    
    public static User toEntityWithPassword(UserDTO userDTO, String password) {
        if (userDTO == null) {
            return null;
        }
        User user = new User(userDTO.getUsername(), userDTO.getEmail(), password);
        user.setId(userDTO.getId());
        return user;
    }

}
