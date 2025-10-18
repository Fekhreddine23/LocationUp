package com.mobility.mobility_backend.dto;

import org.springframework.stereotype.Component;

import com.mobility.mobility_backend.entity.Admin;

@Component
public class AdminMapper {

    public AdminDTO toDTO(Admin admin) {
        if (admin == null) {
            return null;
        }

        return AdminDTO.builder()
                .adminId(admin.getAdminId())
                .username(admin.getUsername())
                .email(admin.getEmail())
                .adminLevel(admin.getAdminLevel())
                .createdAt(admin.getCreatedAt())
                .updatedAt(admin.getUpdatedAt())
                .build();
    }

    public Admin toEntity(AdminDTO adminDTO) {
        if (adminDTO == null) {
            return null;
        }

        return Admin.builder()
                .username(adminDTO.getUsername())
                .email(adminDTO.getEmail())
                .adminLevel(adminDTO.getAdminLevel())
                .build();
    }

    public Admin toEntity(CreateAdminDTO createAdminDTO) {
        if (createAdminDTO == null) {
            return null;
        }

        return Admin.builder()
                .username(createAdminDTO.getUsername())
                .email(createAdminDTO.getEmail())
                .password(createAdminDTO.getPassword()) // ⚠️ Devrait être hashé!
                .adminLevel(createAdminDTO.getAdminLevel())
                .build();
    }

    public void updateEntityFromDTO(AdminDTO adminDTO, Admin admin) {
        if (adminDTO == null || admin == null) {
            return;
        }

        if (adminDTO.getUsername() != null) {
            admin.setUsername(adminDTO.getUsername());
        }
        if (adminDTO.getEmail() != null) {
            admin.setEmail(adminDTO.getEmail());
        }
        if (adminDTO.getAdminLevel() != null) {
            admin.setAdminLevel(adminDTO.getAdminLevel());
        }
    }
}