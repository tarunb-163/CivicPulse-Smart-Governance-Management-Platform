package com.civicpulse.config;

import com.civicpulse.dto.response.ErrorResponse;
import com.civicpulse.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {}) // picked up from CorsFilter bean
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            ErrorResponse errorResponse = ErrorResponse.builder()
                                    .timestamp(Instant.now())
                                    .status(HttpStatus.UNAUTHORIZED.value())
                                    .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                                    .message("Full authentication is required to access this resource")
                                    .path(request.getRequestURI())
                                    .build();
                            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            ErrorResponse errorResponse = ErrorResponse.builder()
                                    .timestamp(Instant.now())
                                    .status(HttpStatus.FORBIDDEN.value())
                                    .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                                    .message("You do not have permission to access this resource")
                                    .path(request.getRequestURI())
                                    .build();
                            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // Public actuator health
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        // Other actuator endpoints require ADMIN
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // Static frontend & documentation
                        .requestMatchers(
                                "/",
                                "/*.html",
                                "/index.html",
                                "/login.html",
                                "/register.html",
                                "/forgot-password.html",
                                "/reset-password.html",
                                "/track.html",
                                "/transparency.html",
                                "/privacy.html",
                                "/terms.html",
                                "/accessibility.html",
                                "/screen-reader.html",
                                "/sitemap.html",
                                "/help.html",
                                "/contact.html",
                                "/404.html",
                                "/403.html",
                                "/500.html",
                                "/citizen/**",
                                "/officer/**",
                                "/admin/**",
                                "/assets/**",
                                "/favicon.ico",
                                "/manifest.json",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Public APIs
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/public/**").permitAll()
                        .requestMatchers("/api/v1/bot/**").permitAll()

                        // Role-specific APIs
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/officer/**").hasAnyRole("OFFICER", "DEPT_HEAD", "ADMIN")

                        // Any other API requires authentication
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
