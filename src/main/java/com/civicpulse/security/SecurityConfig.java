 package com.civicpulse.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    private final RoleBasedAuthenticationProvider roleBasedAuthenticationProvider;


    public SecurityConfig(
            RoleBasedAuthenticationProvider roleBasedAuthenticationProvider) {

        this.roleBasedAuthenticationProvider =
                roleBasedAuthenticationProvider;
    }


    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {


        http


                // =================================================
                // AUTHENTICATION PROVIDER
                // =================================================

                .authenticationProvider(
                        roleBasedAuthenticationProvider
                )


                // =================================================
                // CSRF
                // =================================================
                //
                // CSRF remains enabled.
                //
                // Logout is excluded from CSRF checking so that
                // POST /logout cannot produce HTTP 403.
                //
                // The rest of the application remains protected
                // by CSRF.
                //
                // =================================================

                .csrf(csrf -> csrf

                        .ignoringRequestMatchers(
                                "/logout"
                        )
                )


                // =================================================
                // AUTHORIZATION
                // =================================================

                .authorizeHttpRequests(auth -> auth


                        // -----------------------------------------
                        // PUBLIC PAGES
                        // -----------------------------------------

                        .requestMatchers(
                                "/",
                                "/login",
                                "/officer/login",
                                "/citizen/login",

                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",

                                "/error"
                        )
                        .permitAll()


                        // -----------------------------------------
                        // LOGOUT
                        // -----------------------------------------
                        //
                        // Both Citizen and Officer use:
                        //
                        // POST /logout
                        //
                        // Therefore logout must be accessible
                        // regardless of the user's role.
                        //
                        // -----------------------------------------

                        .requestMatchers(
                                "/logout"
                        )
                        .permitAll()


                        // -----------------------------------------
                        // OFFICER
                        // -----------------------------------------

                        .requestMatchers(
                                "/officer/**"
                        )
                        .hasRole("OFFICER")


                        // -----------------------------------------
                        // CITIZEN
                        // -----------------------------------------

                        .requestMatchers(
                                "/citizen/**"
                        )
                        .hasRole("CITIZEN")


                        // -----------------------------------------
                        // ADMIN
                        // -----------------------------------------

                        .requestMatchers(
                                "/admin/**"
                        )
                        .hasRole("ADMIN")


                        // -----------------------------------------
                        // EVERYTHING ELSE
                        // -----------------------------------------

                        .anyRequest()
                        .authenticated()
                )


                // =================================================
                // FORM LOGIN
                // =================================================

                .formLogin(form -> form


                        // -----------------------------------------
                        // COMMON LOGIN PAGE
                        // -----------------------------------------
                        //
                        // Citizen, Officer and Admin all use:
                        //
                        // templates/officer/login.html
                        //
                        // URL:
                        //
                        // /officer/login
                        //
                        // -----------------------------------------

                        .loginPage(
                                "/officer/login"
                        )


                        // -----------------------------------------
                        // LOGIN PROCESSING
                        // -----------------------------------------
                        //
                        // HTML form:
                        //
                        // POST /login
                        //
                        // -----------------------------------------

                        .loginProcessingUrl(
                                "/login"
                        )


                        // -----------------------------------------
                        // USERNAME
                        // -----------------------------------------

                        .usernameParameter(
                                "username"
                        )


                        // -----------------------------------------
                        // PASSWORD
                        // -----------------------------------------

                        .passwordParameter(
                                "password"
                        )


                        // -----------------------------------------
                        // ROLE
                        // -----------------------------------------
                        //
                        // Reads:
                        //
                        // <select name="role">
                        //
                        // -----------------------------------------

                        .authenticationDetailsSource(
                                new RoleAuthenticationDetailsSource()
                        )


                        // -----------------------------------------
                        // LOGIN SUCCESS
                        // -----------------------------------------

                        .successHandler(
                                authenticationSuccessHandler()
                        )


                        // -----------------------------------------
                        // LOGIN FAILURE
                        // -----------------------------------------

                        .failureUrl(
                                "/officer/login?error=true"
                        )


                        .permitAll()
                )


                // =================================================
                // LOGOUT
                // =================================================

                .logout(logout -> logout


                        // -----------------------------------------
                        // LOGOUT URL
                        // -----------------------------------------
                        //
                        // Citizen:
                        //
                        // POST /logout
                        //
                        // Officer:
                        //
                        // POST /logout
                        //
                        // -----------------------------------------

                        .logoutUrl(
                                "/logout"
                        )


                        // -----------------------------------------
                        // LOGOUT SUCCESS
                        // -----------------------------------------
                        //
                        // IMPORTANT:
                        //
                        // Every role goes to the SAME login page.
                        //
                        // templates/officer/login.html
                        //
                        // -----------------------------------------

                        .logoutSuccessUrl(
                                "/officer/login?logout=true"
                        )


                        // -----------------------------------------
                        // INVALIDATE SESSION
                        // -----------------------------------------

                        .invalidateHttpSession(
                                true
                        )


                        // -----------------------------------------
                        // CLEAR AUTHENTICATION
                        // -----------------------------------------

                        .clearAuthentication(
                                true
                        )


                        // -----------------------------------------
                        // DELETE SESSION COOKIE
                        // -----------------------------------------

                        .deleteCookies(
                                "JSESSIONID"
                        )


                        // -----------------------------------------
                        // LOGOUT ACCESS
                        // -----------------------------------------

                        .permitAll()
                );


        return http.build();
    }


    // =========================================================
    // LOGIN SUCCESS HANDLER
    // =========================================================

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {

        return (request, response, authentication) -> {


            System.out.println(
                    "================================="
            );

            System.out.println(
                    "LOGIN SUCCESS"
            );

            System.out.println(
                    "USERNAME : "
                            + authentication.getName()
            );

            System.out.println(
                    "AUTHORITIES : "
                            + authentication.getAuthorities()
            );

            System.out.println(
                    "================================="
            );


            // =================================================
            // CITIZEN
            // =================================================

            boolean isCitizen =
                    authentication
                            .getAuthorities()
                            .stream()
                            .anyMatch(authority ->
                                    authority
                                            .getAuthority()
                                            .equals("ROLE_CITIZEN")
                            );


            if (isCitizen) {

                response.sendRedirect(
                        "/citizen/dashboard"
                );

                return;
            }


            // =================================================
            // ADMIN
            // =================================================

            boolean isAdmin =
                    authentication
                            .getAuthorities()
                            .stream()
                            .anyMatch(authority ->
                                    authority
                                            .getAuthority()
                                            .equals("ROLE_ADMIN")
                            );


            if (isAdmin) {

                response.sendRedirect(
                        "/admin/dashboard"
                );

                return;
            }


            // =================================================
            // OFFICER
            // =================================================

            boolean isOfficer =
                    authentication
                            .getAuthorities()
                            .stream()
                            .anyMatch(authority ->
                                    authority
                                            .getAuthority()
                                            .equals("ROLE_OFFICER")
                            );


            if (isOfficer) {

                response.sendRedirect(
                        "/officer/dashboard"
                );

                return;
            }


            // =================================================
            // UNKNOWN ROLE
            // =================================================

            response.sendRedirect(
                    "/officer/login?error=true"
            );
        };
    }
}

