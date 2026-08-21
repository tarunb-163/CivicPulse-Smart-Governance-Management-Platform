package com.civicpulse.security;

import com.civicpulse.entites.Admin;
import com.civicpulse.entites.Citizen;
import com.civicpulse.entites.Officer;

import com.civicpulse.repositories.AdminRepository;
import com.civicpulse.repositories.CitizenRepository;
import com.civicpulse.repositories.OfficerRepository;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class RoleBasedAuthenticationProvider
        implements AuthenticationProvider {


    // =========================================================
    // REPOSITORIES
    // =========================================================

    private final OfficerRepository officerRepository;

    private final CitizenRepository citizenRepository;

    private final AdminRepository adminRepository;

    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public RoleBasedAuthenticationProvider(
            OfficerRepository officerRepository,
            CitizenRepository citizenRepository,
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder) {

        this.officerRepository =
                officerRepository;

        this.citizenRepository =
                citizenRepository;

        this.adminRepository =
                adminRepository;

        this.passwordEncoder =
                passwordEncoder;
    }


    // =========================================================
    // AUTHENTICATE
    // =========================================================

    @Override
    public Authentication authenticate(
            Authentication authentication)
            throws AuthenticationException {


        // =====================================================
        // GET USERNAME
        // =====================================================

        String username =
                authentication.getName();


        // =====================================================
        // GET PASSWORD
        // =====================================================

        String password =
                authentication.getCredentials()
                        .toString();


        // =====================================================
        // GET ROLE FROM LOGIN FORM
        // =====================================================

        String role = null;


        if (authentication.getDetails()
                instanceof RoleAuthenticationDetails details) {

            role = details.getRole();
        }


        // =====================================================
        // DEBUG
        // =====================================================

        System.out.println(
                "================================="
        );

        System.out.println(
                "ROLE BASED LOGIN"
        );

        System.out.println(
                "USERNAME : " + username
        );

        System.out.println(
                "ROLE     : " + role
        );

        System.out.println(
                "================================="
        );


        // =====================================================
        // ROLE VALIDATION
        // =====================================================

        if (role == null
                || role.isBlank()) {

            System.out.println(
                    "=== ERROR: ROLE NOT PROVIDED ==="
            );

            throw new BadCredentialsException(
                    "Role is required"
            );
        }


        role = role.toUpperCase();


        // =====================================================
        // CITIZEN LOGIN
        // =====================================================

        if ("CITIZEN".equals(role)) {

            System.out.println(
                    "=== CHECKING CITIZEN TABLE ==="
            );


            Citizen citizen =
                    citizenRepository
                            .findByUsername(username)
                            .orElseThrow(() -> {

                                System.out.println(
                                        "=== CITIZEN NOT FOUND ==="
                                );

                                return new BadCredentialsException(
                                        "Citizen not found"
                                );
                            });


            System.out.println(
                    "=== CITIZEN FOUND: "
                            + citizen.getUsername()
                            + " ==="
            );


            // =================================================
            // CITIZEN ACTIVE CHECK
            // =================================================

            if (!citizen.isActive()) {

                System.out.println(
                        "=== CITIZEN ACCOUNT INACTIVE ==="
                );

                throw new BadCredentialsException(
                        "Citizen account is inactive"
                );
            }


            // =================================================
            // CITIZEN PASSWORD
            //
            // BCrypt
            // =================================================

            if (!passwordEncoder.matches(
                    password,
                    citizen.getPassword())) {

                System.out.println(
                        "=== CITIZEN PASSWORD DOES NOT MATCH ==="
                );

                throw new BadCredentialsException(
                        "Invalid password"
                );
            }


            // =================================================
            // CITIZEN SUCCESS
            // =================================================

            System.out.println(
                    "=== CITIZEN LOGIN SUCCESS ==="
            );


            return new UsernamePasswordAuthenticationToken(

                    citizen.getUsername(),

                    null,

                    Collections.singletonList(
                            new SimpleGrantedAuthority(
                                    "ROLE_CITIZEN"
                            )
                    )
            );
        }


        // =====================================================
        // OFFICER LOGIN
        // =====================================================

        if ("OFFICER".equals(role)) {

            System.out.println(
                    "=== CHECKING OFFICER TABLE ==="
            );


            Officer officer =
                    officerRepository
                            .findByUsername(username)
                            .orElseThrow(() -> {

                                System.out.println(
                                        "=== OFFICER NOT FOUND ==="
                                );

                                return new BadCredentialsException(
                                        "Officer not found"
                                );
                            });


            System.out.println(
                    "=== OFFICER FOUND: "
                            + officer.getUsername()
                            + " ==="
            );


            // =================================================
            // OFFICER ACTIVE CHECK
            // =================================================

            if (!officer.isActive()) {

                System.out.println(
                        "=== OFFICER ACCOUNT INACTIVE ==="
                );

                throw new BadCredentialsException(
                        "Officer account is inactive"
                );
            }


            // =================================================
            // OFFICER PASSWORD
            //
            // BCrypt
            // =================================================

            if (!passwordEncoder.matches(
                    password,
                    officer.getPassword())) {

                System.out.println(
                        "=== OFFICER PASSWORD DOES NOT MATCH ==="
                );

                throw new BadCredentialsException(
                        "Invalid password"
                );
            }


            // =================================================
            // GET OFFICER ROLE
            // =================================================

            String officerRole =
                    officer.getRole();


            if (officerRole == null
                    || officerRole.isBlank()) {

                officerRole = "OFFICER";
            }


            // =================================================
            // OFFICER SUCCESS
            // =================================================

            System.out.println(
                    "=== OFFICER LOGIN SUCCESS ==="
            );

            System.out.println(
                    "OFFICER ROLE : "
                            + officerRole
            );


            return new UsernamePasswordAuthenticationToken(

                    officer.getUsername(),

                    null,

                    Collections.singletonList(
                            new SimpleGrantedAuthority(
                                    "ROLE_"
                                            + officerRole
                                            .toUpperCase()
                            )
                    )
            );
        }


        // =====================================================
        // ADMIN LOGIN
        // =====================================================

        if ("ADMIN".equals(role)) {

            System.out.println(
                    "=== CHECKING ADMIN TABLE ==="
            );


            Admin admin =
                    adminRepository
                            .findByUsername(username)
                            .orElseThrow(() -> {

                                System.out.println(
                                        "=== ADMIN NOT FOUND ==="
                                );

                                return new BadCredentialsException(
                                        "Admin not found"
                                );
                            });


            System.out.println(
                    "=== ADMIN FOUND: "
                            + admin.getUsername()
                            + " ==="
            );


            // =================================================
            // ADMIN ACTIVE CHECK
            // =================================================

            if (!admin.isActive()) {

                System.out.println(
                        "=== ADMIN ACCOUNT INACTIVE ==="
                );

                throw new BadCredentialsException(
                        "Admin account is inactive"
                );
            }


            // =================================================
            // ADMIN PASSWORD
            //
            // IMPORTANT:
            //
            // ADMIN USES PLAIN TEXT
            // NO BCrypt
            // =================================================

            System.out.println(
                    "=== CHECKING ADMIN PASSWORD ==="
            );


            if (!password.equals(admin.getPassword())) {

                System.out.println(
                        "=== ADMIN PASSWORD DOES NOT MATCH ==="
                );

                throw new BadCredentialsException(
                        "Invalid password"
                );
            }


            System.out.println(
                    "=== ADMIN PASSWORD MATCHED ==="
            );


            // =================================================
            // ADMIN ROLE CHECK
            // =================================================

            if (admin.getRole() == null
                    || !admin.getRole()
                    .equalsIgnoreCase("ADMIN")) {

                System.out.println(
                        "=== USER IS NOT AN ADMIN ==="
                );

                throw new BadCredentialsException(
                        "User is not an administrator"
                );
            }


            // =================================================
            // ADMIN SUCCESS
            // =================================================

            System.out.println(
                    "=== ADMIN LOGIN SUCCESS ==="
            );


            return new UsernamePasswordAuthenticationToken(

                    admin.getUsername(),

                    null,

                    Collections.singletonList(
                            new SimpleGrantedAuthority(
                                    "ROLE_ADMIN"
                            )
                    )
            );
        }


        // =====================================================
        // INVALID ROLE
        // =====================================================

        System.out.println(
                "=== INVALID ROLE: "
                        + role
                        + " ==="
        );


        throw new BadCredentialsException(
                "Invalid role"
        );
    }


    // =========================================================
    // SUPPORTS
    // =========================================================

    @Override
    public boolean supports(
            Class<?> authentication) {

        return UsernamePasswordAuthenticationToken.class
                .isAssignableFrom(authentication);
    }
}

