package com.example.account.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${account.jwt.secret}")
    private String secret;

    @Value("${account.jwt.expirationSeconds}")
    private long expirationSeconds;

    private Key key;

    private void ensureKeyInitialized() {
        if (this.key != null) return;
        // Use the configured secret to build a key. If secret length insufficient, derive via HMAC SHA key generator
        if (secret == null) {
            secret = "default-change-me-secret-for-dev-only";
        }
        if (secret.length() < 32) {
            key = Keys.hmacShaKeyFor((secret + "-padding-for-dev-only-please-change").getBytes());
        } else {
            key = Keys.hmacShaKeyFor(secret.getBytes());
        }
    }

    public String generateToken(String subject, Map<String, Object> claims) {
    ensureKeyInitialized();
    // if expirationSeconds not set or non-positive, default to 3600 seconds for tests/dev
    if (this.expirationSeconds <= 0) this.expirationSeconds = 3600;
    long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expiry = new Date(now + expirationSeconds * 1000);

        return Jwts.builder()
                .setClaims(claims != null ? claims : new HashMap<>())
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims getClaims(String token) {
        ensureKeyInitialized();
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        if (token == null) return false;
        try {
            Claims claims = getClaims(token);
            Date exp = claims.getExpiration();
            return exp != null && exp.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
