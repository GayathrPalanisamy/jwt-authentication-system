package com.onedao.authentication.controller;

import com.onedao.authentication.dto.AuthResponse;
import com.onedao.authentication.dto.LoginRequest;
import com.onedao.authentication.dto.RegisterRequest;
import com.onedao.authentication.service.AuthService;
import com.onedao.authentication.service.CountryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.onedao.authentication.security.JwtService;
import com.onedao.authentication.security.TokenRevocationService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final TokenRevocationService tokenRevocationService;
    private final CountryService countryService;

    public AuthController(AuthService authService,
                          JwtService jwtService,
                          TokenRevocationService tokenRevocationService,
                          CountryService countryService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.tokenRevocationService = tokenRevocationService;
        this.countryService = countryService;
    }

    //Register a user
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = httpRequest.getRemoteAddr();

        countryService.validateCountry(clientIp); //mock test

        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Registration successful",
                        "username", request.username()
                ));
    }

    //Authenticate and receive a JWT
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            Authentication authentication) {

        String token = authorization.substring(7);

        var claims = jwtService.extractAllClaims(token);

        tokenRevocationService.revokeToken(
                claims.getId(),
                claims.getExpiration().toInstant()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        authentication.getName() + " Logged Out"
                )
        );
    }
}
