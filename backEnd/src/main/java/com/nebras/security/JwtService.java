package com.nebras.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final long hours;

    public JwtService(@Value("${nebras.jwt.secret}") String secret, @Value("${nebras.jwt.hours}") long hours) {
        byte[] b = secret.getBytes(StandardCharsets.UTF_8);
        if (b.length < 32) throw new IllegalStateException("JWT_SECRET must be at least 32 bytes");
        this.key = Keys.hmacShaKeyFor(b);
        this.hours = hours;
    }

    public String create(long id, String role) {
        Date now = new Date();
        return Jwts.builder().subject(String.valueOf(id)).claim("role", role)
                .issuedAt(now).expiration(new Date(now.getTime() + hours * 3_600_000L))
                .signWith(key).compact();
    }

    public AuthUser parse(String token) {
        Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return new AuthUser(Long.parseLong(c.getSubject()), c.get("role", String.class));
    }
}
