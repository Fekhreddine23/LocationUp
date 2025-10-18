package com.mobility.mobility_backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobility.mobility_backend.dto.AdminDTO;
import com.mobility.mobility_backend.dto.AdminMapper;
import com.mobility.mobility_backend.dto.CreateAdminDTO;
import com.mobility.mobility_backend.entity.Admin;
import com.mobility.mobility_backend.repository.AdminRepository;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

	private final AdminRepository adminRepository;
	private final AdminMapper adminMapper;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public AdminServiceImpl(AdminRepository adminRepository, AdminMapper adminMapper, PasswordEncoder passwordEncoder) {
		this.adminRepository = adminRepository;
		this.adminMapper = adminMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional(readOnly = true)
	public List<AdminDTO> getAllAdmins() {
		return adminRepository.findAll().stream().map(adminMapper::toDTO).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<AdminDTO> getAdminById(Integer id) {
		return adminRepository.findById(id).map(adminMapper::toDTO);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<AdminDTO> getAdminByUsername(String username) {
		return adminRepository.findByUsername(username).map(adminMapper::toDTO);
	}

	@Override
	public AdminDTO createAdmin(CreateAdminDTO createAdminDTO) {
		// Vérifier si l'username ou email existe déjà
		if (adminRepository.existsByUsername(createAdminDTO.getUsername())) {
			throw new IllegalArgumentException("Un admin avec ce nom d'utilisateur existe déjà");
		}
		if (adminRepository.existsByEmail(createAdminDTO.getEmail())) {
			throw new IllegalArgumentException("Un admin avec cet email existe déjà");
		}

		// Hasher le mot de passe
		String hashedPassword = passwordEncoder.encode(createAdminDTO.getPassword());

		// Créer l'admin avec le mot de passe hashé
		Admin admin = new Admin();
		admin.setUsername(createAdminDTO.getUsername());
		admin.setEmail(createAdminDTO.getEmail());
		admin.setPassword(hashedPassword);
		admin.setAdminLevel(createAdminDTO.getAdminLevel());

		Admin savedAdmin = adminRepository.save(admin);
		return adminMapper.toDTO(savedAdmin);
	}

	@Override
	public Optional<AdminDTO> updateAdmin(Integer id, AdminDTO adminDTO) {
		Optional<Admin> existingAdmin = adminRepository.findById(id);
		if (existingAdmin.isPresent()) {
			Admin admin = existingAdmin.get();

			// Mettre à jour les champs
			if (adminDTO.getUsername() != null) {
				admin.setUsername(adminDTO.getUsername());
			}
			if (adminDTO.getEmail() != null) {
				admin.setEmail(adminDTO.getEmail());
			}
			if (adminDTO.getAdminLevel() != null) {
				admin.setAdminLevel(adminDTO.getAdminLevel());
			}

			Admin updatedAdmin = adminRepository.save(admin);
			return Optional.of(adminMapper.toDTO(updatedAdmin));
		}
		return Optional.empty();
	}

	@Override
	public boolean deleteAdmin(Integer id) {
		if (adminRepository.existsById(id)) {
			adminRepository.deleteById(id);
			return true;
		}
		return false;
	}

	@Override
	@Transactional(readOnly = true)
	public boolean adminExists(Integer id) {
		return adminRepository.existsById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean usernameExists(String username) {
		return adminRepository.existsByUsername(username);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean emailExists(String email) {
		return adminRepository.existsByEmail(email);
	}
}