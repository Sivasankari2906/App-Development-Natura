package com.examly.plantcare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.examly.plantcare.dto.AuthResponse;
import com.examly.plantcare.dto.LoginRequest;
import com.examly.plantcare.dto.RegisterRequest;
import com.examly.plantcare.dto.ResetPasswordRequest;
import com.examly.plantcare.entity.User;
import com.examly.plantcare.repository.UserRepository;
import com.examly.plantcare.util.JwtUtil;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .gardeningExperience(request.getGardeningExperience())
                .location(request.getLocation())
                .timezone(request.getTimezone())
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getRole().name());

        return new AuthResponse(token, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) throw new RuntimeException("User not found");

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getRole().name());

        return new AuthResponse(token, refreshToken);
    }

    public String logout(String token) {
        // Add token to revoked table if needed
        return "Logged out successfully.";
    }

    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        String role = jwtUtil.getRoleFromToken(refreshToken);
        String newAccessToken = jwtUtil.generateToken(username, role);
        String newRefreshToken = jwtUtil.generateRefreshToken(username, role);
        return new AuthResponse(newAccessToken, newRefreshToken);
    }


    public String forgotPassword(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) throw new RuntimeException("Email not registered");

        String resetToken = UUID.randomUUID().toString();
        // Normally save resetToken in DB & email it
        return "Reset token: " + resetToken;
    }

    public String resetPassword(ResetPasswordRequest request) {
        // Validate token here
        return "Password reset successful for token: " + request.getToken();
    }

    public String verifyEmail(String token) {
        // Token validation logic
        return "Email verified successfully.";
    }
}
