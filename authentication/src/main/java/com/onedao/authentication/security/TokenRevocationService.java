package com.onedao.authentication.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenRevocationService {

    private static final Logger logger =
            LoggerFactory.getLogger(TokenRevocationService.class);

    // Token ID -> expiration time
    private final Map<String, Instant> revokedTokens =
            new ConcurrentHashMap<>();

    public void revokeToken(String tokenId, Instant expiration) {

        revokedTokens.put(tokenId, expiration);

        logger.info("JWT revoked successfully");
    }

    public boolean isRevoked(String tokenId) {

        Instant expiration = revokedTokens.get(tokenId);

        if (expiration == null) {
            return false;
        }

        // Remove expired entries when encountered
        if (!expiration.isAfter(Instant.now())) {

            revokedTokens.remove(tokenId, expiration);

            logger.debug("Expired token revocation entry cleaned up");

            return false;
        }

        logger.debug("JWT rejected because it has been revoked");

        return true;
    }
}
