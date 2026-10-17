package com.onedao.authentication.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenRevocationService {

    // Token ID -> expiration time
    private final Map<String, Instant> revokedTokens =
            new ConcurrentHashMap<>();

    public void revokeToken(String tokenId, Instant expiration) {
        revokedTokens.put(tokenId, expiration);
    }

    public boolean isRevoked(String tokenId) {
        Instant expiration = revokedTokens.get(tokenId);

        if (expiration == null) {
            return false;
        }

        // Remove expired entries when encountered
        if (!expiration.isAfter(Instant.now())) {
            revokedTokens.remove(tokenId, expiration);
            return false;
        }

        return true;
    }
}

