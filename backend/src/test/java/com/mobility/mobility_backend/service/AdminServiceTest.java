package com.mobility.mobility_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.mobility.mobility_backend.dto.AdminDTO;
import com.mobility.mobility_backend.dto.AdminMapper;
import com.mobility.mobility_backend.dto.CreateAdminDTO;
import com.mobility.mobility_backend.entity.Admin;
import com.mobility.mobility_backend.repository.AdminRepository;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("ci")
class AdminServiceImplTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AdminMapper adminMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void whenGetAllAdmins_thenReturnAdminList() {
        // Given
        Admin admin1 = new Admin();
        Admin admin2 = new Admin();
        List<Admin> admins = Arrays.asList(admin1, admin2);

        AdminDTO dto1 = createAdminDTO(1);
        AdminDTO dto2 = createAdminDTO(2);

        when(adminRepository.findAll()).thenReturn(admins);
        when(adminMapper.toDTO(admin1)).thenReturn(dto1);
        when(adminMapper.toDTO(admin2)).thenReturn(dto2);

        // When
        List<AdminDTO> result = adminService.getAllAdmins();

        // Then
        assertThat(result).hasSize(2);
        verify(adminRepository, times(1)).findAll();
    }

    @Test
    void whenGetExistingAdminById_thenReturnAdmin() {
        // Given
        Integer adminId = 1;
        Admin admin = new Admin();
        AdminDTO adminDTO = createAdminDTO(adminId);

        when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(adminMapper.toDTO(admin)).thenReturn(adminDTO);

        // When
        Optional<AdminDTO> result = adminService.getAdminById(adminId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getAdminId()).isEqualTo(adminId);
    }

    @Test
    void whenCreateValidAdmin_thenAdminIsSaved() {
        // Given
        CreateAdminDTO createAdminDTO = new CreateAdminDTO("admin1", "admin@test.com", "password", 1);
        Admin admin = new Admin();
        Admin savedAdmin = new Admin();
        AdminDTO adminDTO = createAdminDTO(1);

        when(adminRepository.existsByUsername("admin1")).thenReturn(false);
        when(adminRepository.existsByEmail("admin@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
        when(adminRepository.save(any(Admin.class))).thenReturn(savedAdmin);
        when(adminMapper.toDTO(savedAdmin)).thenReturn(adminDTO);

        // When
        AdminDTO result = adminService.createAdmin(createAdminDTO);

        // Then
        assertThat(result).isEqualTo(adminDTO);
        verify(adminRepository, times(1)).save(any(Admin.class));
    }

    @Test
    void whenCreateAdminWithExistingUsername_thenThrowException() {
        // Given
        CreateAdminDTO createAdminDTO = new CreateAdminDTO("admin1", "admin@test.com", "password", 1);
        when(adminRepository.existsByUsername("admin1")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> adminService.createAdmin(createAdminDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Un admin avec ce nom d'utilisateur existe déjà");
    }

    @Test
    void whenDeleteExistingAdmin_thenReturnTrue() {
        // Given
        Integer adminId = 1;
        when(adminRepository.existsById(adminId)).thenReturn(true);

        // When
        boolean result = adminService.deleteAdmin(adminId);

        // Then
        assertThat(result).isTrue();
        verify(adminRepository, times(1)).deleteById(adminId);
    }

    private AdminDTO createAdminDTO(Integer id) {
        AdminDTO dto = new AdminDTO();
        dto.setAdminId(id);
        dto.setUsername("testadmin");
        dto.setEmail("admin@test.com");
        dto.setAdminLevel(1);
        return dto;
    }
}