
package com.onedao.authentication.security;

import com.onedao.authentication.entity.AppUser;
import com.onedao.authentication.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

        System.out.println("JWT FILTER REQUEST: "
                + request.getMethod() + " " + request.getRequestURI());

        response.setHeader("X-Debug-Backend", "jwt-filter-v1");

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        System.out.println("Bearer token present: "
                + (header != null && header.startsWith("Bearer ")));

        try {
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);

                boolean valid = jwtService.isTokenValid(token);
                System.out.println("JWT valid: " + valid);

                if (!valid) {
                    sendUnauthorized(response);
                    return;
                }

                String tokenId = jwtService.extractTokenId(token);

                if (tokenRevocationService.isRevoked(tokenId)) {
                    System.out.println("JWT rejected: token revoked");
                    sendUnauthorized(response);
                    return;
                }

                String username = jwtService.extractUsername(token);

                AppUser user = userRepository.findByUsername(username)
                        .orElse(null);

                if (user == null || !user.isEnabled()) {
                    System.out.println("JWT rejected: user missing or disabled");
                    sendUnauthorized(response);
                    return;
                }

                var authentication =
                        new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                List.of(new SimpleGrantedAuthority(
                                        "ROLE_" + user.getRole().name()))
                        );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);

                System.out.println("Authenticated username: " + username);
                System.out.println("Authorities: "
                        + authentication.getAuthorities());
            }
        } catch (JwtException | IllegalArgumentException ex) {
            System.out.println("JWT ERROR TYPE: "
                    + ex.getClass().getSimpleName());
            System.out.println("JWT ERROR MESSAGE: " + ex.getMessage());

            sendUnauthorized(response);
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            System.out.println("JWT FILTER FINAL STATUS: "
                    + response.getStatus());
        }
    }

    private void sendUnauthorized(HttpServletResponse response)
            throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"message\":\"Invalid or expired token\"}");
    }
}