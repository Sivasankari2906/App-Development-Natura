package com.examly.plantcare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.examly.plantcare.dto.AuthResponse;
import com.examly.plantcare.dto.LoginRequest;
import com.examly.plantcare.dto.RegisterRequest;
import com.examly.plantcare.dto.ResetPasswordRequest;
import com.examly.plantcare.entity.PasswordResetToken;
import com.examly.plantcare.entity.User;
import com.examly.plantcare.repository.PasswordResetTokenRepository;
import com.examly.plantcare.repository.UserRepository;
import com.examly.plantcare.util.JwtUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PasswordResetTokenRepository resetTokens;
    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long RESET_TOKEN_MINUTES = 30;

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
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getRole().name(), passwordVersion(user));

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
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getRole().name(), passwordVersion(user));

        return new AuthResponse(token, refreshToken);
    }

    public String logout(String token) {
        // Add token to revoked table if needed
        return "Logged out successfully.";
    }

    public AuthResponse refreshToken(String header) {
        String token = header != null && header.startsWith("Bearer ") ? header.substring(7) : header;
        if (token == null || !jwtUtil.isTokenValid(token) || !jwtUtil.isRefreshToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token.");
        }
        User user = userRepository.findByUsername(jwtUtil.getUsernameFromToken(token))
                .filter(u -> !Boolean.FALSE.equals(u.getIsActive()))
                .filter(u -> passwordVersion(u).equals(jwtUtil.getPasswordVersion(token)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token."));
        return new AuthResponse(
                jwtUtil.generateToken(user.getUsername(), user.getRole().name()),
                jwtUtil.generateRefreshToken(user.getUsername(), user.getRole().name(), passwordVersion(user)));
    }

    /** Changes whenever the password changes, so older refresh tokens stop working. */
    private static String passwordVersion(User user) {
        return hash(user.getPasswordHash()).substring(0, 16);
    }

    @Transactional
    public void updateNotificationPreferences(String username, Boolean email, Boolean careReminders) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in."));
        java.util.Map<String, Object> prefs = new java.util.LinkedHashMap<>();
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            prefs.putAll(mapper.convertValue(
                    com.examly.plantcare.util.NotificationPrefs.parse(mapper, user.getNotificationPreferences()),
                    java.util.Map.class));
            if (email != null) prefs.put("email", email);
            if (careReminders != null) prefs.put("careReminders", careReminders);
            user.setNotificationPreferences(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(prefs));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid notification preferences.");
        }
        userRepository.save(user);
    }

    @Transactional
    public String forgotPassword(String email) {
        // Same answer whether or not the email exists, so accounts cannot be discovered.
        String response = "If that email is registered, a password reset link has been sent.";
        if (email == null || email.isBlank()) {
            return response;
        }
        userRepository.findByEmail(email.trim()).ifPresent(user -> {
            resetTokens.deleteByUser(user);

            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setTokenHash(hash(rawToken));
            token.setExpiresAt(LocalDateTime.now().plusMinutes(RESET_TOKEN_MINUTES));
            resetTokens.save(token);

            String link = frontendUrl + "/reset-password?token=" + rawToken;
            emailService.send(user.getEmail(), "Reset your Natura password",
                    "Hi " + user.getUsername() + ",\n\nUse this link to choose a new password. "
                            + "It expires in " + RESET_TOKEN_MINUTES + " minutes and works once:\n" + link
                            + "\n\nIf you did not ask for this, you can ignore this email.");
        });
        return response;
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        if (request.getToken() == null || request.getToken().isBlank()
                || request.getNewPassword() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token and new password are required.");
        }
        if (!isStrongPassword(request.getNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be 8+ characters with uppercase, lowercase, a number and a special character.");
        }
        PasswordResetToken token = resetTokens.findByTokenHash(hash(request.getToken()))
                .filter(t -> !t.isUsed() && t.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "This reset link is invalid or has expired."));

        if (resetTokens.markUsed(token.getId()) != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This reset link is invalid or has expired.");
        }
        token.setUsed(true);
        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "Password has been reset. You can now log in.";
    }

    private static boolean isStrongPassword(String password) {
        return password.length() >= 8
                && password.chars().anyMatch(Character::isUpperCase)
                && password.chars().anyMatch(Character::isLowerCase)
                && password.chars().anyMatch(Character::isDigit)
                && password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
    }

    private static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public String verifyEmail(String token) {
        // Token validation logic
        return "Email verified successfully.";
    }
}
