package com.examly.plantcare.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final Key key;
    private final long accessExpiryMs;
    private final long refreshExpiryMs;

    public JwtUtil(
            @Value("${jwt.secret}") String base64Secret,
            @Value("${jwt.access.expiration-ms:3600000}") long accessExpiryMs,         // 1h
            @Value("${jwt.refresh.expiration-ms:604800000}") long refreshExpiryMs     // 7d
    ) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(base64Secret));
        this.accessExpiryMs = accessExpiryMs;
        this.refreshExpiryMs = refreshExpiryMs;
    }

    public String generateToken(String username, String role) {
        return buildToken(username, role, accessExpiryMs, "access", null);
    }

    // Include role in refresh token too (your AuthService reads role from it)
    public String generateRefreshToken(String username, String role) {
        return buildToken(username, role, refreshExpiryMs, "refresh", null);
    }

    /** Refresh token bound to a password version, so a password change invalidates it. */
    public String generateRefreshToken(String username, String role, String passwordVersion) {
        return buildToken(username, role, refreshExpiryMs, "refresh", passwordVersion);
    }

    private String buildToken(String username, String role, long ttlMs, String type, String passwordVersion) {
        long now = System.currentTimeMillis();
        Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("role", role);
        claims.put("typ", type);
        if (passwordVersion != null) {
            claims.put("pv", passwordVersion);
        }
        return Jwts.builder()
                .setSubject(username)
                .addClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ttlMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public String getRoleFromToken(String token) {
        Object r = getAllClaims(token).get("role");
        return r == null ? null : r.toString();
    }

    public boolean isAccessToken(String token) {
        return "access".equals(getAllClaims(token).get("typ"));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(getAllClaims(token).get("typ"));
    }

    public String getPasswordVersion(String token) {
        Object v = getAllClaims(token).get("pv");
        return v == null ? null : v.toString();
    }

    public boolean isTokenValid(String token) {
        try {
            getAllClaims(token); // parses & validates signature + exp
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private <T> T getClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(getAllClaims(token));
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
