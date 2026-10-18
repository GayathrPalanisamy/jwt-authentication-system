package com.onedao.authentication.config;

import com.onedao.authentication.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private static final Logger logger =
            LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.info("Initializing BCrypt password encoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        logger.info("Configuring Spring Security filter chain");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/api/settings/**")
                        .hasRole("ADMINISTRATOR")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response,
                                                   authException) -> {

                            logger.warn(
                                    "Authentication required for {} {}",
                                    request.getMethod(),
                                    request.getRequestURI()
                            );

                            response.setStatus(
                                    HttpServletResponse.SC_UNAUTHORIZED
                            );
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"message\":\"Authentication required\"}"
                            );
                        })
                        .accessDeniedHandler((request, response,
                                              accessDeniedException) -> {

                            logger.warn(
                                    "Access denied for {} {}",
                                    request.getMethod(),
                                    request.getRequestURI()
                            );

                            response.setStatus(
                                    HttpServletResponse.SC_FORBIDDEN
                            );
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"message\":\"Access denied\"}"
                            );
                        })
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        logger.info("Spring Security filter chain configured successfully");

        return http.build();
    }
}
