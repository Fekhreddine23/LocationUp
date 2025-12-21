package com.mobility.mobility_backend.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.mobility.mobility_backend.dto.user.AvatarResponse;
import com.mobility.mobility_backend.entity.Admin;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.AdminRepository;
import com.mobility.mobility_backend.repository.UserRepository;

@Service
public class UserAvatarService {

	private final UserRepository userRepository;
	private final AdminRepository adminRepository;
	private final Path storageLocation;

	public UserAvatarService(UserRepository userRepository, AdminRepository adminRepository,
			@Value("${app.avatar.storage-dir:uploads/avatars}") String storageDir) {
		this.userRepository = userRepository;
		this.adminRepository = adminRepository;
		this.storageLocation = Paths.get(storageDir).toAbsolutePath().normalize();
		try {
			Files.createDirectories(storageLocation);
		} catch (IOException ex) {
			throw new IllegalStateException("Impossible de créer le répertoire de stockage des avatars", ex);
		}
	}

	public AvatarResponse uploadAvatar(User user, MultipartFile file) {
		String filename = storeFile(file, user.getId());
		deleteFile(user.getAvatarPath());
		user.setAvatarPath(filename);
		userRepository.save(user);
		return buildResponse(filename);
	}

	public AvatarResponse uploadAdminAvatar(Admin admin, MultipartFile file) {
		String filename = storeFile(file, admin.getAdminId());
		deleteFile(admin.getAvatarPath());
		admin.setAvatarPath(filename);
		adminRepository.save(admin);
		return buildResponse(filename);
	}

	public AvatarResponse getAvatarMetadata(User user) {
		if (user.getAvatarPath() == null || user.getAvatarPath().isBlank()) {
			return null;
		}
		return buildResponse(user.getAvatarPath());
	}

	public AvatarResponse getAdminAvatarMetadata(Admin admin) {
		if (admin.getAvatarPath() == null || admin.getAvatarPath().isBlank()) {
			return null;
		}
		return buildResponse(admin.getAvatarPath());
	}

	public void deleteAvatar(User user) {
		deleteFile(user.getAvatarPath());
		user.setAvatarPath(null);
		userRepository.save(user);
	}

	public void deleteAdminAvatar(Admin admin) {
		deleteFile(admin.getAvatarPath());
		admin.setAvatarPath(null);
		adminRepository.save(admin);
	}

	public Resource loadAsResource(String filename) {
		try {
			Path file = storageLocation.resolve(filename).normalize();
			validatePath(file);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() && resource.isReadable()) {
				return resource;
			}
			throw new IllegalStateException("Fichier non trouvé");
		} catch (MalformedURLException ex) {
			throw new IllegalStateException("Fichier non trouvable", ex);
		}
	}

	private void deleteFile(String filename) {
		if (filename == null || filename.isBlank()) {
			return;
		}
		try {
			Path file = storageLocation.resolve(filename).normalize();
			validatePath(file);
			Files.deleteIfExists(file);
		} catch (IOException ignored) {
		}
	}

	private void validatePath(Path file) {
		if (!file.startsWith(storageLocation)) {
			throw new IllegalArgumentException("Chemin de fichier invalide");
		}
	}

	private String storeFile(MultipartFile file, Integer ownerId) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Fichier avatar vide");
		}
		String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
		if (extension == null || extension.isBlank()) {
			extension = "png";
		}
		String cleanExtension = extension.toLowerCase();
		String filename = "owner-" + ownerId + "-" + System.currentTimeMillis() + "." + cleanExtension;
		Path targetLocation = storageLocation.resolve(filename).normalize();
		validatePath(targetLocation);
		try {
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex) {
			throw new IllegalStateException("Impossible d'enregistrer l'avatar", ex);
		}
		return filename;
	}

	private AvatarResponse buildResponse(String filename) {
		return new AvatarResponse(filename, "/api/users/avatar/" + filename);
	}
}
