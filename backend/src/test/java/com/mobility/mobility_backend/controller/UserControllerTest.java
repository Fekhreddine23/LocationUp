package com.mobility.mobility_backend.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.mobility.mobility_backend.dto.UserDTO;
import com.mobility.mobility_backend.service.UserService;

@WebMvcTest(UserController.class)
@TestPropertySource(properties = {
	    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration",
	    "spring.security.enabled=false"
	})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void testCreateUser() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO(1L, "john_doe", "john@example.com");
        when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(userDTO);

        // When & Then
        mockMvc.perform(post("/api/users")
                .param("password", "password123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"john_doe\", \"email\": \"john@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    public void testGetAllUsers() throws Exception {
        // Given
        UserDTO user1 = new UserDTO(1L, "john_doe", "john@example.com");
        UserDTO user2 = new UserDTO(2L, "jane_smith", "jane@example.com");
        when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("john_doe"))
                .andExpect(jsonPath("$[1].username").value("jane_smith"));
    }

    @Test
    public void testGetUserById() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO(1L, "john_doe", "john@example.com");
        when(userService.getUserById(1L)).thenReturn(Optional.of(userDTO));

        // When & Then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("john_doe"));
    }

    @Test
    public void testGetUserByIdNotFound() throws Exception {
        // Given
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetUserByUsername() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO(1L, "john_doe", "john@example.com");
        when(userService.getUserByUsername("john_doe")).thenReturn(Optional.of(userDTO));

        // When & Then
        mockMvc.perform(get("/api/users/username/john_doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john_doe"));
    }
}