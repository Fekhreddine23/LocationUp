package com.mobility.mobility_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobility.mobility_backend.dto.UserDTO;
import com.mobility.mobility_backend.entity.Role;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.UserRepository;

@Service
@Transactional
public class AdminUserService {

    private final UserRepository userRepository;

    @Autowired
    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDTO> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<UserDTO> getUserById(Integer id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    public UserDTO changeUserRole(Integer userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));

        try {
            // Convertir le String en Enum Role
            Role role = Role.valueOf(newRole.toUpperCase());
            user.setRole(role);
            User updatedUser = userRepository.save(user);
            return convertToDTO(updatedUser);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rôle invalide: " + newRole + ". Rôles valides: ROLE_USER, ROLE_ADMIN");
        }
    }

    public UserDTO updateUser(Integer userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));

        // Mettre à jour les champs modifiables
        if (userDTO.getUsername() != null) {
            user.setUsername(userDTO.getUsername());
        }
        if (userDTO.getFirstname() != null) {  // Ajouté pour firstname
            user.setFirstName(userDTO.getFirstname());
        }
        if (userDTO.getLastname() != null) {
            user.setLastName(userDTO.getLastname());  // Corrigé : setLastname au lieu de setLastName
        }
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getRole() != null) {
            try {
                Role role = Role.valueOf(userDTO.getRole().toUpperCase());
                user.setRole(role);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Rôle invalide: " + userDTO.getRole());
            }
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    public void deactivateUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));
        // Si vous avez un champ active, sinon gérer autrement
        // user.setActive(false);
        userRepository.save(user);
    }

    public void activateUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));
        // Si vous avez un champ active
        // user.setActive(true);
        userRepository.save(user);
    }

    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public List<UserDTO> searchUsers(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findByEmailContainingOrFirstNameContainingOrLastNameContaining(
                query, query, query, pageable);
        return userPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name()); // Convertir Enum en String
        dto.setFirstname(user.getFirstName());
        dto.setLastname(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());       // Convertir Enum en String si nécessaire
        return dto;
    }
}