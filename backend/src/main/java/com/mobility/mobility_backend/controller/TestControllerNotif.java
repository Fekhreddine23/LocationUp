package com.mobility.mobility_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
public class TestControllerNotif {

	@GetMapping("/security")
    public String testSecurity() {
        return "✅ Security config is working!";
    }

    @GetMapping("/public")
    public String testPublic() {
        return "✅ Public endpoint accessible!";
    }

}
