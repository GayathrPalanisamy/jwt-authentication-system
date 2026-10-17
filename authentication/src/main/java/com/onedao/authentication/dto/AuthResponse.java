package com.onedao.authentication.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
