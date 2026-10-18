package com.onedao.authentication.security;

import com.onedao.authentication.entity.AppUser;
import com.onedao.authentication.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenRevocationService tokenRevocationService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository,
            TokenRevocationService tokenRevocationService) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.tokenRevocationService = tokenRevocationService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        logger.debug(
                "JWT filter processing {} {}",
                request.getMethod(),
                request.getRequestURI()
        );

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        try {
            if (header != null && header.startsWith("Bearer ")) {

                String token = header.substring(7);

                boolean valid = jwtService.isTokenValid(token);

                if (!valid) {
                    logger.warn("JWT validation failed");
                    sendUnauthorized(response);
                    return;
                }

                String tokenId = jwtService.extractTokenId(token);

                if (tokenRevocationService.isRevoked(tokenId)) {
                    logger.warn("JWT rejected because the token has been revoked");
                    sendUnauthorized(response);
                    return;
                }

                String username = jwtService.extractUsername(token);

                AppUser user = userRepository.findByUsername(username)
                        .orElse(null);

                if (user == null || !user.isEnabled()) {
                    logger.warn(
                            "JWT rejected because the user is missing or disabled"
                    );
                    sendUnauthorized(response);
                    return;
                }

                var authentication =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_" + user.getRole().name()
                                        )
                                )
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

                logger.debug(
                        "JWT authentication successful for username: {}",
                        username
                );
            }
        } catch (JwtException | IllegalArgumentException ex) {

            // Do not log the token or exception message, which may expose
            // sensitive information.
            logger.warn(
                    "JWT processing failed: {}",
                    ex.getClass().getSimpleName()
            );

            sendUnauthorized(response);
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            logger.debug(
                    "Request completed: method={}, path={}, status={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus()
            );
        }
    }

    private void sendUnauthorized(HttpServletResponse response)
            throws IOException {

        SecurityContextHolder.clearContext();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"message\":\"Invalid or expired token\"}"
        );
    }
}
