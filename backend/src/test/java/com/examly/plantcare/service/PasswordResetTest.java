package com.examly.plantcare.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.examly.plantcare.dto.ResetPasswordRequest;
import com.examly.plantcare.entity.PasswordResetToken;
import com.examly.plantcare.entity.User;
import com.examly.plantcare.repository.PasswordResetTokenRepository;
import com.examly.plantcare.repository.UserRepository;
import com.examly.plantcare.util.JwtUtil;

@ExtendWith(MockitoExtension.class)
class PasswordResetTest {

    @Mock UserRepository userRepository;
    @Mock PasswordResetTokenRepository resetTokens;
    @Mock EmailService emailService;
    @Mock JwtUtil jwtUtil;
    PasswordEncoder encoder = new BCryptPasswordEncoder();
    AuthService authService;
    User user;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, encoder, jwtUtil, resetTokens, emailService);
        ReflectionTestUtils.setField(authService, "frontendUrl", "http://app.test");
        user = User.builder().id(1L).username("rose").email("rose@example.com")
                .passwordHash(encoder.encode("OldPassw0rd!")).build();
    }

    private ResetPasswordRequest request(String token, String password) {
        ResetPasswordRequest r = new ResetPasswordRequest();
        r.setToken(token);
        r.setNewPassword(password);
        return r;
    }

    /** Runs forgotPassword and returns the raw token from the emailed link. */
    private String requestResetAndCaptureToken() {
        when(userRepository.findByEmail("rose@example.com")).thenReturn(Optional.of(user));
        authService.forgotPassword("rose@example.com");
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(emailService).send(eq("rose@example.com"), anyString(), body.capture());
        String text = body.getValue();
        int i = text.indexOf("token=") + 6;
        return text.substring(i, text.indexOf('\n', i));
    }

    @Test
    void unknownEmailGetsSameAnswerAndSendsNothing() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        String answer = authService.forgotPassword("nobody@example.com");

        assertTrue(answer.startsWith("If that email is registered"));
        verifyNoInteractions(emailService);
        verify(resetTokens, never()).save(any());
    }

    @Test
    void forgotPasswordStoresOnlyAHashAndEmailsTheLink() {
        String raw = requestResetAndCaptureToken();

        ArgumentCaptor<PasswordResetToken> saved = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(resetTokens).save(saved.capture());
        assertNotEquals(raw, saved.getValue().getTokenHash());
        assertEquals(64, saved.getValue().getTokenHash().length());
        assertTrue(saved.getValue().getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void validTokenChangesPasswordAndIsSingleUse() {
        String raw = requestResetAndCaptureToken();
        ArgumentCaptor<PasswordResetToken> saved = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(resetTokens).save(saved.capture());
        PasswordResetToken token = saved.getValue();
        when(resetTokens.findByTokenHash(token.getTokenHash())).thenReturn(Optional.of(token));

        authService.resetPassword(request(raw, "NewPassw0rd!"));

        assertTrue(encoder.matches("NewPassw0rd!", user.getPasswordHash()));
        assertTrue(token.isUsed());
        ResponseStatusException reuse = assertThrows(ResponseStatusException.class,
                () -> authService.resetPassword(request(raw, "AnotherPassw0rd!")));
        assertEquals(HttpStatus.BAD_REQUEST, reuse.getStatus());
    }

    @Test
    void expiredTokenIsRejected() {
        String raw = requestResetAndCaptureToken();
        ArgumentCaptor<PasswordResetToken> saved = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(resetTokens).save(saved.capture());
        PasswordResetToken token = saved.getValue();
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(resetTokens.findByTokenHash(token.getTokenHash())).thenReturn(Optional.of(token));

        assertThrows(ResponseStatusException.class, () -> authService.resetPassword(request(raw, "NewPassw0rd!")));
        assertTrue(encoder.matches("OldPassw0rd!", user.getPasswordHash()));
    }

    @Test
    void unknownTokenAndWeakPasswordAreRejected() {
        when(resetTokens.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> authService.resetPassword(request("bogus", "NewPassw0rd!")));
        assertThrows(ResponseStatusException.class, () -> authService.resetPassword(request("bogus", "weak")));
    }
}
