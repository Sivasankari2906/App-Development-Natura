package com.examly.plantcare.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.examly.plantcare.entity.User;
import com.examly.plantcare.repository.UserRepository;

/**
 * Creates the first admin account from environment variables, because ADMIN
 * cannot be chosen at registration. Does nothing unless all three are set.
 */
@Configuration
public class AdminBootstrap {

    @Bean
    CommandLineRunner createInitialAdmin(
            UserRepository users,
            PasswordEncoder encoder,
            @Value("${app.admin.username:}") String username,
            @Value("${app.admin.email:}") String email,
            @Value("${app.admin.password:}") String password) {
        return args -> {
            if (username.isBlank() || email.isBlank() || password.isBlank()) {
                return;
            }
            if (users.existsByUsername(username) || users.existsByEmailIgnoreCase(email)) {
                return;
            }
            users.save(User.builder()
                    .username(username)
                    .email(email)
                    .passwordHash(encoder.encode(password))
                    .role(User.Role.ADMIN)
                    .build());
        };
    }
}
