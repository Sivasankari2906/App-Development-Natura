package com.examly.plantcare.service;

import com.examly.plantcare.entity.User;
import com.examly.plantcare.repository.UserRepository;
import com.examly.plantcare.repository.PlantRepository;
import com.examly.plantcare.repository.CareTaskRepository;
import com.examly.plantcare.repository.HealthRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PlantRepository plantRepository;
    
    @Autowired
    private CareTaskRepository careTaskRepository;
    
    @Autowired
    private HealthRecordRepository healthRecordRepository;

    public Page<User> getUsers(Pageable pageable, String role, String status, String search) {
        try {
            if (search != null && !search.isEmpty()) {
                return userRepository.findByUsernameContainingIgnoreCase(search, pageable);
            }
            if (role != null && !role.isEmpty() && !role.equals("All Roles")) {
                return userRepository.findByRole(User.Role.valueOf(role), pageable);
            }
            return userRepository.findAll(pageable);
        } catch (Exception e) {
            // Return empty page if there's an error
            return userRepository.findAll(PageRequest.of(0, 0));
        }
    }

    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("activeToday", userRepository.countByLastLoginAfter(LocalDateTime.now().minusDays(1)));
        stats.put("newThisMonth", userRepository.countByCreatedDateAfter(LocalDateTime.now().minusMonths(1)));
        stats.put("premiumUsers", userRepository.countByRole(User.Role.PREMIUM_PLANT_OWNER));
        return stats;
    }

    public Map<String, Object> getSystemAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Performance metrics (simulated - in real app, get from system monitoring)
        Map<String, Object> performance = new HashMap<>();
        performance.put("cpuUsage", Math.random() * 100);
        performance.put("memoryUsage", Math.random() * 100);
        performance.put("diskUsage", Math.random() * 100);
        analytics.put("performance", performance);
        
        // User activity
        Map<String, Object> activity = new HashMap<>();
        activity.put("activeSessions", userRepository.countByLastLoginAfter(LocalDateTime.now().minusHours(1)));
        activity.put("pageViews", (int)(Math.random() * 50000) + 10000);
        activity.put("apiCalls", (int)(Math.random() * 20000) + 5000);
        activity.put("errorRate", Math.random() * 2);
        analytics.put("activity", activity);
        
        // Database stats
        Map<String, Object> database = new HashMap<>();
        long totalRecords = userRepository.count() + plantRepository.count();
        if (careTaskRepository != null) {
            totalRecords += careTaskRepository.count();
        }
        if (healthRecordRepository != null) {
            totalRecords += healthRecordRepository.count();
        }
        database.put("totalRecords", totalRecords);
        database.put("queryPerformance", 95 + Math.random() * 5);
        database.put("backupStatus", "Healthy");
        analytics.put("database", database);
        
        // Security metrics
        Map<String, Object> security = new HashMap<>();
        security.put("failedLogins", (int)(Math.random() * 50));
        security.put("blockedIPs", (int)(Math.random() * 10));
        security.put("sslStatus", "Active");
        analytics.put("security", security);
        
        return analytics;
    }

    public Map<String, Object> getSystemPerformance() {
        Map<String, Object> performance = new HashMap<>();
        
        // System metrics (in real app, integrate with system monitoring tools)
        performance.put("uptime", "99.9%");
        performance.put("responseTime", "120ms");
        performance.put("throughput", "1,234 req/min");
        performance.put("errorCount", 12);
        
        return performance;
    }

    public Map<String, Object> getAvailableReports() {
        Map<String, Object> reports = new HashMap<>();
        
        // User reports
        Map<String, Object> userReports = new HashMap<>();
        userReports.put("userActivity", "User engagement and activity metrics");
        userReports.put("registrationTrends", "New user registration patterns");
        reports.put("userReports", userReports);
        
        // System reports
        Map<String, Object> systemReports = new HashMap<>();
        systemReports.put("performanceAnalytics", "System performance metrics");
        systemReports.put("errorLogs", "System errors and issues");
        reports.put("systemReports", systemReports);
        
        // Business reports
        Map<String, Object> businessReports = new HashMap<>();
        businessReports.put("revenueAnalytics", "Financial performance metrics");
        businessReports.put("plantCareStats", "Plant health and care metrics");
        reports.put("businessReports", businessReports);
        
        return reports;
    }

    public User updateUserStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        // In real app, implement status update logic
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}