package com.mobility.mobility_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mobility.mobility_backend.dto.driver.DriverProfileDTO;
import com.mobility.mobility_backend.entity.DriverProfile;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.repository.DriverProfileRepository;
import com.mobility.mobility_backend.repository.UserRepository;

@Service
@Transactional
public class DriverProfileService {

	private final DriverProfileRepository driverProfileRepository;
	private final UserRepository userRepository;

	public DriverProfileService(DriverProfileRepository driverProfileRepository, UserRepository userRepository) {
		this.driverProfileRepository = driverProfileRepository;
		this.userRepository = userRepository;
	}

	public DriverProfileDTO getProfile(Integer userId) {
		if (userId == null) {
			return null;
		}
		return driverProfileRepository.findByUser_Id(userId).map(this::toDto).orElse(null);
	}

	public DriverProfileDTO saveProfile(Integer userId, DriverProfileDTO payload) {
		if (userId == null) {
			throw new IllegalArgumentException("Utilisateur requis");
		}
		DriverProfile profile = driverProfileRepository.findByUser_Id(userId)
				.orElseGet(() -> buildProfile(userId));
		apply(profile, payload);
		return toDto(driverProfileRepository.save(profile));
	}

	public DriverProfileDTO getProfileSnapshot(Integer userId) {
		return getProfile(userId);
	}

	private DriverProfile buildProfile(Integer userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
		DriverProfile profile = new DriverProfile();
		profile.setUser(user);
		return profile;
	}

	private void apply(DriverProfile profile, DriverProfileDTO payload) {
		if (payload == null) {
			return;
		}
		profile.setLicenseNumber(payload.getLicenseNumber());
		profile.setLicenseCountry(payload.getLicenseCountry());
		profile.setLicenseCategory(payload.getLicenseCategory());
		profile.setLicenseIssuedOn(payload.getLicenseIssuedOn());
		profile.setLicenseExpiresOn(payload.getLicenseExpiresOn());
		profile.setAnnualKilometers(payload.getAnnualKilometers());
		profile.setUsageReason(payload.getUsageReason());
		profile.setNotes(payload.getNotes());
	}

	private DriverProfileDTO toDto(DriverProfile profile) {
		if (profile == null) {
			return null;
		}
		DriverProfileDTO dto = new DriverProfileDTO();
		dto.setId(profile.getId());
		if (profile.getUser() != null) {
			dto.setUserId(profile.getUser().getId());
		}
		dto.setLicenseNumber(profile.getLicenseNumber());
		dto.setLicenseCountry(profile.getLicenseCountry());
		dto.setLicenseCategory(profile.getLicenseCategory());
		dto.setLicenseIssuedOn(profile.getLicenseIssuedOn());
		dto.setLicenseExpiresOn(profile.getLicenseExpiresOn());
		dto.setAnnualKilometers(profile.getAnnualKilometers());
		dto.setUsageReason(profile.getUsageReason());
		dto.setNotes(profile.getNotes());
		dto.setUpdatedAt(profile.getUpdatedAt());
		return dto;
	}
}
