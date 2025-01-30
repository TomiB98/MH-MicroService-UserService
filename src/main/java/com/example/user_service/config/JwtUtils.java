package com.example.user_service.config;

import com.example.user_service.models.RoleType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    private final SecretKey secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    public JwtUtils(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateToken(String username, Map<String, String> claims) {
        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    public String createToken(String username, Long id, RoleType roleType){
        Map<String, String> claims = new HashMap<>();
        claims.put("id", id.toString());
        claims.put("email", username);
        claims.put("role", roleType.toString());
        return generateToken(username, claims);
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject(); // Extracts the user email
    }

    public Long extractUserId(String token) {
        return Long.valueOf(parseClaims(token).get("id", String.class)); // Extracts the user id
    }

    public String extractUserRole(String token) {
        return parseClaims(token).get("role", String.class); // Extracts the user role
    }

    public boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }
}
