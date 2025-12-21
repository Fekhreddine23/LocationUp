package com.mobility.mobility_backend.service.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.UUID;
import java.util.Comparator;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorageService {

	private static final Set<String> AUTHORIZED_CONTENT_TYPES = Set.of(
			"image/jpeg", "image/png", "image/webp", "image/jpg");
	private static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
	private static final long MAX_DIRECTORY_SIZE_BYTES = 500L * 1024L * 1024L; // 500 MB
	private final Path offersDirectory;

	public ImageStorageService() {
		Path uploadsRoot = Paths.get("uploads").toAbsolutePath().normalize();
		this.offersDirectory = uploadsRoot.resolve("offers");
		createDirectoriesIfNeeded(this.offersDirectory);
	}

	public String storeOfferImage(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("Aucun fichier fourni");
		}

		if (file.getSize() > MAX_FILE_SIZE_BYTES) {
			throw new IllegalArgumentException("Le fichier est trop volumineux (max 5MB)");
		}

		String contentType = file.getContentType();
		if (contentType == null || !AUTHORIZED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
			throw new IllegalArgumentException("Format d'image non supporté (JPEG, PNG, WEBP)");
		}

		if (!isValidSignature(file)) {
			throw new IllegalArgumentException("Signature de fichier non valide");
		}

		if (getDirectorySize(offersDirectory) > MAX_DIRECTORY_SIZE_BYTES) {
			throw new IllegalStateException("Quota de stockage dépassé pour les uploads");
		}

		String extension = resolveExtension(file.getOriginalFilename(), contentType);
		String filename = UUID.randomUUID() + extension;
		Path target = offersDirectory.resolve(filename);

		try {
			file.transferTo(target.toFile());
			trySetSafePermissions(target);
			enforceQuota();
		} catch (IOException ex) {
			throw new RuntimeException("Impossible de sauvegarder l'image: " + ex.getMessage(), ex);
		}

		// URL relative servie via le ResourceHandler (/uploads/**)
		return "/uploads/offers/" + filename;
	}

	private void createDirectoriesIfNeeded(Path directory) {
		try {
			Files.createDirectories(directory);
		} catch (IOException e) {
			throw new RuntimeException("Impossible de préparer le dossier de stockage: " + e.getMessage(), e);
		}
	}

	private String resolveExtension(String originalFilename, String contentType) {
		String extFromName = StringUtils.getFilenameExtension(originalFilename);
		if (extFromName != null && !extFromName.isBlank()) {
			return "." + extFromName.replaceAll("[^a-zA-Z0-9]", "");
		}
		if (contentType.contains("png")) return ".png";
		if (contentType.contains("webp")) return ".webp";
		return ".jpg";
	}

	private boolean isValidSignature(MultipartFile file) {
		try {
			byte[] header = file.getBytes();
			return isPng(header) || isJpeg(header) || isWebp(header);
		} catch (IOException e) {
			return false;
		}
	}

	private boolean isPng(byte[] bytes) {
		byte[] sig = new byte[] {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
		return bytes != null && bytes.length >= sig.length && java.util.Arrays.equals(java.util.Arrays.copyOf(bytes, sig.length), sig);
	}

	private boolean isJpeg(byte[] bytes) {
		return bytes != null && bytes.length >= 2 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8;
	}

	private boolean isWebp(byte[] bytes) {
		if (bytes == null || bytes.length < 12) return false;
		return bytes[0]=='R' && bytes[1]=='I' && bytes[2]=='F' && bytes[3]=='F'
				&& bytes[8]=='W' && bytes[9]=='E' && bytes[10]=='B' && bytes[11]=='P';
	}

	private long getDirectorySize(Path directory) {
		try {
			return Files.walk(directory)
					.filter(Files::isRegularFile)
					.mapToLong(p -> {
						try {
							return Files.size(p);
						} catch (IOException e) {
							return 0L;
						}
					})
					.sum();
		} catch (IOException e) {
			return 0L;
		}
	}

	private void enforceQuota() {
		long size = getDirectorySize(offersDirectory);
		if (size <= MAX_DIRECTORY_SIZE_BYTES) {
			return;
		}
		try {
			var files = Files.list(offersDirectory)
					.filter(Files::isRegularFile)
					.sorted(Comparator.comparingLong(p -> {
						try {
							return Files.getLastModifiedTime(p).toMillis();
						} catch (IOException e) {
							return Long.MAX_VALUE;
						}
					}))
					.toList();
			for (Path p : files) {
				try {
					Files.deleteIfExists(p);
					size = getDirectorySize(offersDirectory);
					if (size <= MAX_DIRECTORY_SIZE_BYTES) {
						return;
					}
				} catch (IOException e) {
					// ignore and continue
				}
			}
			if (size > MAX_DIRECTORY_SIZE_BYTES) {
				throw new IllegalStateException("Quota de stockage dépassé, purge impossible");
			}
		} catch (IOException e) {
			throw new RuntimeException("Erreur lors du nettoyage du répertoire uploads: " + e.getMessage(), e);
		}
	}

	private void trySetSafePermissions(Path target) {
		try {
			if (Files.getFileStore(target).supportsFileAttributeView("posix")) {
				Set<PosixFilePermission> perms = Set.of(
						PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
						PosixFilePermission.GROUP_READ,
						PosixFilePermission.OTHERS_READ
				);
				Files.setPosixFilePermissions(target, perms);
			}
		} catch (Exception ignored) {
		}
	}
}
