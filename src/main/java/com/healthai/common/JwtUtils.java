package com.healthai.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    @Value("${healthai.jwt.secret-key:healthai-default-secret-key-change-me}")
    private String secret;
    private static SecretKey KEY;
    private static final long TTL = 7L * 24 * 60 * 60 * 1000;

    @PostConstruct
    public void init() {
        KEY = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    //生成token,再放 userId、username
    public static String generateToken(Long userId, String username) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TTL))
                .signWith(KEY)
                .compact();
    }

    public static Claims parse(String token) {
        try {
            return Jwts.parser().verifyWith(KEY).build()
                    .parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public static Long getUserId(String token) {
        Claims c = parse(token);
        return c == null ? null : c.get("userId", Long.class);
    }

    public static String getUsername(String token) {
        Claims c = parse(token);
        return c == null ? null : c.getSubject();
    }

    public static boolean isExpired(String token) {
        Claims c = parse(token);
        return c == null || c.getExpiration().before(new Date());
    }
}
