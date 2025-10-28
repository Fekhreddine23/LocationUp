package com.mobility.mobility_backend.controller;

 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    public TestController() {
        System.out.println("🎉 [TestController] NEW CONTROLLER INITIALIZED!");
    }

    @PostMapping("/public")
    public ResponseEntity<Map<String, Object>> testPublic() {
        System.out.println("✅ [TestController] PUBLIC POST endpoint called!");
        return ResponseEntity.ok(Map.of(
            "message", "TestController public POST endpoint works!",
            "timestamp", LocalDateTime.now().toString(),
            "controller", "TestController"
        ));
    }

    @GetMapping("/public") 
    public ResponseEntity<Map<String, Object>> testPublicGet() {
        System.out.println("✅ [TestController] PUBLIC GET endpoint called!");
        return ResponseEntity.ok(Map.of(
            "message", "TestController GET endpoint works!", 
            "timestamp", LocalDateTime.now().toString(),
            "controller", "TestController"
        ));
    }
}