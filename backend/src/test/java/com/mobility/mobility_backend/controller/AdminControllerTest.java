package com.mobility.mobility_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobility.mobility_backend.dto.AdminDTO;
import com.mobility.mobility_backend.dto.CreateAdminDTO;
import com.mobility.mobility_backend.service.AdminService;

@WebMvcTest(AdminController.class)
@TestPropertySource(properties = {
	    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration,org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration",
	    "spring.security.enabled=false"
	})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Autowired
    private ObjectMapper objectMapper;

    // ========== TESTS GET ==========


    //Récupérer tous les admins = /api/admins
    @Test
    void whenGetAllAdmins_thenReturnAdmins() throws Exception {
        // Given
        AdminDTO admin1 = createAdminDTO(1, "admin1", "admin1@test.com");
        AdminDTO admin2 = createAdminDTO(2, "admin2", "admin2@test.com");
        List<AdminDTO> admins = Arrays.asList(admin1, admin2);

        when(adminService.getAllAdmins()).thenReturn(admins);

        // When & Then
        mockMvc.perform(get("/api/admins"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].adminId").value(1))
                .andExpect(jsonPath("$[0].username").value("admin1"))
                .andExpect(jsonPath("$[1].adminId").value(2))
                .andExpect(jsonPath("$[1].username").value("admin2"));
    }


    //Récupérer un admin par ID existant = /api/admins/{id}
    @Test
    void whenGetExistingAdminById_thenReturnAdmin() throws Exception {
        // Given
        AdminDTO admin = createAdminDTO(1, "admin1", "admin1@test.com");
        when(adminService.getAdminById(1)).thenReturn(Optional.of(admin));

        // When & Then
        mockMvc.perform(get("/api/admins/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adminId").value(1))
                .andExpect(jsonPath("$.username").value("admin1"))
                .andExpect(jsonPath("$.email").value("admin1@test.com"));
    }


    // /api/admins/{id}	ID inexistant → 404
    @Test
    void whenGetNonExistingAdminById_thenReturnNotFound() throws Exception {
        // Given
        when(adminService.getAdminById(999)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/admins/999"))
                .andExpect(status().isNotFound());
    }


    //Recherche par username existant =  /api/admins/username/{username}
    @Test
    void whenGetAdminByExistingUsername_thenReturnAdmin() throws Exception {
        // Given
        AdminDTO admin = createAdminDTO(1, "admin1", "admin1@test.com");
        when(adminService.getAdminByUsername("admin1")).thenReturn(Optional.of(admin));

        // When & Then
        mockMvc.perform(get("/api/admins/username/admin1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin1"));
    }


    //Username inexistant → 404 = /api/admins/username/{username}

    @Test
    void whenGetAdminByNonExistingUsername_thenReturnNotFound() throws Exception {
        // Given
        when(adminService.getAdminByUsername("unknown")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/admins/username/unknown"))
                .andExpect(status().isNotFound());
    }

    // ========== TESTS POST ==========


    //Création réussie → 201
    @Test
    void whenCreateValidAdmin_thenReturnCreated() throws Exception {
        // Given
        CreateAdminDTO createAdminDTO = new CreateAdminDTO("newadmin", "new@test.com", "password123", 1);
        AdminDTO createdAdmin = createAdminDTO(1, "newadmin", "new@test.com");

        when(adminService.createAdmin(any(CreateAdminDTO.class))).thenReturn(createdAdmin);

        // When & Then
        mockMvc.perform(post("/api/admins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAdminDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.adminId").value(1))
                .andExpect(jsonPath("$.username").value("newadmin"));
    }


    //Username existant → 400
    @Test
    void whenCreateAdminWithExistingUsername_thenReturnBadRequest() throws Exception {
        // Given
        CreateAdminDTO createAdminDTO = new CreateAdminDTO("existing", "test@test.com", "password", 1);

        when(adminService.createAdmin(any(CreateAdminDTO.class)))
                .thenThrow(new IllegalArgumentException("Un admin avec ce nom d'utilisateur existe déjà"));

        // When & Then
        mockMvc.perform(post("/api/admins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAdminDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Un admin avec ce nom d'utilisateur existe déjà"));
    }

    // ========== TESTS PUT ==========


    //Mise à jour réussie → 200
    @Test
    void whenUpdateExistingAdmin_thenReturnUpdatedAdmin() throws Exception {
        // Given
        AdminDTO adminDTO = createAdminDTO(1, "updated", "updated@test.com");
        AdminDTO updatedAdmin = createAdminDTO(1, "updated", "updated@test.com");

        when(adminService.updateAdmin(eq(1), any(AdminDTO.class)))
                .thenReturn(Optional.of(updatedAdmin));

        // When & Then
        mockMvc.perform(put("/api/admins/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adminId").value(1))
                .andExpect(jsonPath("$.username").value("updated"));
    }


    //ID inexistant → 404
    @Test
    void whenUpdateNonExistingAdmin_thenReturnNotFound() throws Exception {
        // Given
        AdminDTO adminDTO = createAdminDTO(999, "unknown", "unknown@test.com");
        when(adminService.updateAdmin(eq(999), any(AdminDTO.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/admins/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminDTO)))
                .andExpect(status().isNotFound());
    }

    // ========== TESTS DELETE ==========


    //Suppression réussie → 204
    @Test
    void whenDeleteExistingAdmin_thenReturnNoContent() throws Exception {
        // Given
        when(adminService.deleteAdmin(1)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/admins/1"))
                .andExpect(status().isNoContent());
    }


    //ID inexistant → 404
    @Test
    void whenDeleteNonExistingAdmin_thenReturnNotFound() throws Exception {
        // Given
        when(adminService.deleteAdmin(999)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/admins/999"))
                .andExpect(status().isNotFound());
    }

    // ========== TESTS DE VERIFICATION ==========


    //ID inexistant → 404
    @Test
    void whenCheckExistingUsername_thenReturnTrue() throws Exception {
        // Given
        when(adminService.usernameExists("existing")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/admins/check-username/existing"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }


    //Username n'existe pas → false
    @Test
    void whenCheckNonExistingUsername_thenReturnFalse() throws Exception {
        // Given
        when(adminService.usernameExists("nonexisting")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/admins/check-username/nonexisting"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }


    //	Email existe → true
    @Test
    void whenCheckExistingEmail_thenReturnTrue() throws Exception {
        // Given
        when(adminService.emailExists("existing@test.com")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/admins/check-email/existing@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }


    //Email n'existe pas → false

    @Test
    void whenCheckNonExistingEmail_thenReturnFalse() throws Exception {
        // Given
        when(adminService.emailExists("nonexisting@test.com")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/admins/check-email/nonexisting@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    // ========== METHODE UTILITAIRE ==========

    private AdminDTO createAdminDTO(Integer id, String username, String email) {
        AdminDTO dto = new AdminDTO();
        dto.setAdminId(id);
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setAdminLevel(1);
        return dto;
    }
}