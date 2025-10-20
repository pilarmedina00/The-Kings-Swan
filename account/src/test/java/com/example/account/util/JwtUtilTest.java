package com.example.account.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    @Test
    public void generateAndValidateToken() {
        JwtUtil util = new JwtUtil();
        // rely on defaults for secret and expiration (may be 0 if not set) -- ensure expirationSeconds set via reflection isn't necessary for this test because validateToken checks expiry
        String token = util.generateToken("sub", new HashMap<>());
        assertNotNull(token);

        Claims claims = util.getClaims(token);
        assertEquals("sub", claims.getSubject());
        assertTrue(util.validateToken(token));
    }
}
