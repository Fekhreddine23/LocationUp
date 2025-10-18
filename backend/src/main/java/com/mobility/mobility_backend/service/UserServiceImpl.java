package com.mobility.mobility_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mobility.mobility_backend.dto.UserDTO;
import com.mobility.mobility_backend.dto.UserMapper;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDTO createUser(String username, String email, String password) {
        // Vérifier si l'username existe déjà
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Un utilisateur avec le nom '" + username + "' existe déjà");
        }

        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Un utilisateur avec l'email '" + email + "' existe déjà");
        }

        // Créer et sauvegarder le nouvel utilisateur
        User user = new User(username, email, password);
        User savedUser = userRepository.save(user);

        // Convertir en DTO et retourner
        return UserMapper.toDTO(savedUser);
    }

    @Override
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toDTO);
    }

    @Override
    public Optional<UserDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserMapper::toDTO);
    }

    @Override
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserMapper::toDTO);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
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
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
