package com.onedao.authentication.service;

import com.onedao.authentication.dto.AuthResponse;
import com.onedao.authentication.dto.LoginRequest;
import com.onedao.authentication.dto.RegisterRequest;
import com.onedao.authentication.entity.AppUser;
import com.onedao.authentication.entity.Role;
import com.onedao.authentication.repository.UserRepository;
import com.onedao.authentication.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final Logger logger =
            LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;

        logger.info("AuthService initialized");
    }

    @Transactional
    public void register(RegisterRequest request) {

        logger.info(
                "Processing registration for username: {}",
                request.username()
        );

        if (userRepository.existsByUsername(request.username())) {
            logger.warn("Registration rejected: username already exists");

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username already exists"
            );
        }

        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            logger.warn("Registration rejected: email already exists");

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        AppUser user = new AppUser();

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(
                passwordEncoder.encode(request.password()));
        user.setRole(Role.STANDARD_USER);

        userRepository.save(user);

        logger.info(
                "User registered successfully: {}",
                user.getUsername()
        );
    }

    public AuthResponse login(LoginRequest request) {

        logger.info(
                "Processing login for username: {}",
                request.username()
        );

        AppUser user = userRepository
                .findByUsername(request.username())
                .orElseThrow(() -> {
                    logger.warn("Login failed: invalid credentials");
                    return new BadCredentialsException("Login Failed");
                });

        if (!user.isEnabled()
                || !passwordEncoder.matches(
                request.password(), user.getPassword())) {

            logger.warn("Login failed: invalid credentials");

            throw new BadCredentialsException("Login Failed");
        }

        String token = jwtService.generateToken(user);

        logger.info(
                "Login successful for username: {}",
                user.getUsername()
        );

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs() / 1000
        );
    }
}
