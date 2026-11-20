package com.examly.plantcare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        validateRegistration(request);

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

    private void validateRegistration(RegisterRequest request) {
        String username = request.getUsername();
        String email = request.getEmail();
        if (username == null || username.isBlank() || email == null || email.isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()
                || request.getRole() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Username, email, password and role are required.");
        }
        if (request.getRole() == User.Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Admin accounts cannot be created through registration.");
        }
        if (username.length() > 50 || email.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Username must be at most 50 characters and email at most 100.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered.");
        }
    }

    public AuthResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
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
