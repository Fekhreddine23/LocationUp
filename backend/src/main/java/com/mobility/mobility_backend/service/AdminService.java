package com.mobility.mobility_backend.service;

import java.util.List;
import java.util.Optional;

import com.mobility.mobility_backend.dto.AdminDTO;
import com.mobility.mobility_backend.dto.CreateAdminDTO;

public interface AdminService {

	// Récupérer tous les administrateurs
	List<AdminDTO> getAllAdmins();

	// Récupérer un administrateur par ID
	Optional<AdminDTO> getAdminById(Integer id);

	// Récupérer un administrateur par nom d'utilisateur
	Optional<AdminDTO> getAdminByUsername(String username);

	// Créer un nouvel administrateur
	AdminDTO createAdmin(CreateAdminDTO createAdminDTO);

	// Mettre à jour un administrateur
	Optional<AdminDTO> updateAdmin(Integer id, AdminDTO adminDTO);

	// Supprimer un administrateur
	boolean deleteAdmin(Integer id);

	// Vérifier si un administrateur existe
	boolean adminExists(Integer id);

	// Vérifier si un nom d'utilisateur existe déjà
	boolean usernameExists(String username);

	// Vérifier si un email existe déjà
	boolean emailExists(String email);
}