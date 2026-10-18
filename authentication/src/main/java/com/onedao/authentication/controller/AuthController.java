package com.onedao.authentication.controller;

import com.onedao.authentication.dto.AuthResponse;
import com.onedao.authentication.dto.LoginRequest;
import com.onedao.authentication.dto.RegisterRequest;
import com.onedao.authentication.security.JwtService;
import com.onedao.authentication.security.TokenRevocationService;
import com.onedao.authentication.service.AuthService;
import com.onedao.authentication.service.CountryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger =
            LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtService jwtService;
    private final TokenRevocationService tokenRevocationService;
    private final CountryService countryService;

    public AuthController(
            AuthService authService,
            JwtService jwtService,
            TokenRevocationService tokenRevocationService,
            CountryService countryService
    ) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.tokenRevocationService = tokenRevocationService;
        this.countryService = countryService;

        logger.info("AuthController initialized");
    }

    // Register a user
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {

        String clientIp = httpRequest.getRemoteAddr();

        logger.info("Registration request received for username: {}",
                request.username());

        countryService.validateCountry(clientIp);

        authService.register(request);

        logger.info("Registration successful for username: {}",
                request.username());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Registration successful",
                        "username", request.username()
                ));
    }

    // Authenticate and receive a JWT
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        logger.info("Login attempt received for username: {}",
                request.username());

        AuthResponse response = authService.login(request);

        logger.info("Login successful for username: {}",
                request.username());

        return ResponseEntity.ok(response);
    }

    // Revoke the current JWT
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            Authentication authentication
    ) {

        String username = authentication.getName();

        logger.info("Logout request received for username: {}", username);

        String token = authorization.substring(7);

        var claims = jwtService.extractAllClaims(token);

        tokenRevocationService.revokeToken(
                claims.getId(),
                claims.getExpiration().toInstant()
        );

        logger.info("Logout successful for username: {}", username);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        username + " Logged Out"
                )
        );
    }
}
