package com.onedao.authentication.security;

import com.onedao.authentication.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final Logger logger =
            LoggerFactory.getLogger(JwtService.class);

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret-base64}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {

        this.signingKey = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret));

        this.expirationMs = expirationMs;

        logger.info(
                "JwtService initialized; token expiration configured to {} ms",
                expirationMs
        );
    }

    public String generateToken(AppUser user) {
        Instant now = Instant.now();

        String token = Jwts.builder()
                .subject(user.getUsername())
                .claim("role", user.getRole().name())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(
                        now.plusMillis(expirationMs)))
                .signWith(signingKey)
                .compact();

        logger.debug(
                "JWT generated successfully for username: {}",
                user.getUsername()
        );

        return token;
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);

            boolean valid = claims.getSubject() != null
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date())
                    && claims.getId() != null;

            if (!valid) {
                logger.debug("JWT rejected because required claims are missing or expired");
            }

            return valid;

        } catch (JwtException | IllegalArgumentException ex) {
            logger.debug(
                    "JWT validation failed: {}",
                    ex.getClass().getSimpleName()
            );

            return false;
        }
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractTokenId(String token) {
        return extractAllClaims(token).getId();
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
