package com.mobility.mobility_backend.controller;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
public class debugController {

    public debugController() {
        System.out.println("🎉 [DebugController] INITIALIZED!");
    }

    @PostMapping("/test-post")
    public ResponseEntity<Map<String, Object>> testPost() {
        System.out.println("✅ [DebugController] POST endpoint called!");
        return ResponseEntity.ok(Map.of(
            "message", "DebugController POST works!",
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/test-get")
    public ResponseEntity<Map<String, Object>> testGet() {
        System.out.println("✅ [DebugController] GET endpoint called!");
        return ResponseEntity.ok(Map.of(
            "message", "DebugController GET works!",
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}