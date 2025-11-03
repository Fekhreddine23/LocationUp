package com.mobility.mobility_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobility.mobility_backend.dto.AdminStatsDTO;
import com.mobility.mobility_backend.dto.RecentActivityDTO;
import com.mobility.mobility_backend.entity.User;
import com.mobility.mobility_backend.service.DashboardService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {


	private final DashboardService dashboardService;

    @Autowired
    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getDashboardStats() {
        AdminStatsDTO stats = dashboardService.getAdminStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Page<User>> getUsers(Pageable pageable) {
        Page<User> users = dashboardService.getUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<List<RecentActivityDTO>> getRecentActivity() {
        List<RecentActivityDTO> activity = dashboardService.getRecentActivity();
        return ResponseEntity.ok(activity);
    }

}
