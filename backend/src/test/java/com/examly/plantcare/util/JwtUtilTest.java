package com.examly.plantcare.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.SecureRandom;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static String newSecret() {
        byte[] bytes = new byte[48];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(newSecret(), 60_000L, 120_000L);
    }

    @Test
    void accessTokenCarriesUsernameAndRole() {
        String token = jwtUtil.generateToken("rose", "ADMIN");

        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals("rose", jwtUtil.getUsernameFromToken(token));
        assertEquals("ADMIN", jwtUtil.getRoleFromToken(token));
    }

    @Test
    void refreshTokenCarriesUsernameAndRole() {
        String token = jwtUtil.generateRefreshToken("fern", "USER");

        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals("fern", jwtUtil.getUsernameFromToken(token));
        assertEquals("USER", jwtUtil.getRoleFromToken(token));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtUtil shortLived = new JwtUtil(newSecret(), -1_000L, -1_000L);

        assertFalse(shortLived.isTokenValid(shortLived.generateToken("rose", "USER")));
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtUtil other = new JwtUtil(newSecret(), 60_000L, 120_000L);

        assertFalse(jwtUtil.isTokenValid(other.generateToken("rose", "USER")));
    }

    @Test
    void malformedTokenIsRejected() {
        assertFalse(jwtUtil.isTokenValid("not-a-jwt"));
    }
}
