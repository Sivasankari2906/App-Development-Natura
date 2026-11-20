package com.examly.plantcare.controller;

import com.examly.plantcare.entity.User;
import com.examly.plantcare.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public Page<User> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size);
        return adminService.getUsers(pageable, role, status, search);
    }

    @GetMapping("/users/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        return ResponseEntity.ok(adminService.getUserStats());
    }

    @GetMapping("/system/analytics")
    public ResponseEntity<Map<String, Object>> getSystemAnalytics() {
        return ResponseEntity.ok(adminService.getSystemAnalytics());
    }

    @GetMapping("/system/performance")
    public ResponseEntity<Map<String, Object>> getSystemPerformance() {
        return ResponseEntity.ok(adminService.getSystemPerformance());
    }

    @GetMapping("/reports/list")
    public ResponseEntity<Map<String, Object>> getAvailableReports() {
        return ResponseEntity.ok(adminService.getAvailableReports());
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<User> updateUserStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, status));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}