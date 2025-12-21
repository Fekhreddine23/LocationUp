package com.mobility.mobility_backend.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TempPasswordController {

    @GetMapping("/generate-password")
    public String generatePassword(@RequestParam String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(password);

        return "Raw: " + password + "\n" +
               "BCrypt: " + hash + "\n" +
               "SQL: INSERT INTO admins (username, email, password, role, admin_level) VALUES " +
               "('admin', 'admin@mobility.com', '" + hash + "', 'ROLE_ADMIN', 10)";
    }
}
