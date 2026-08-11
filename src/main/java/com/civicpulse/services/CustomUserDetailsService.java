package com.civicpulse.services;

import com.civicpulse.entites.Officer;
import com.civicpulse.repositories.OfficerRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final OfficerRepository officerRepository;

    public CustomUserDetailsService(OfficerRepository officerRepository) {
        this.officerRepository = officerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("=== LOGIN ATTEMPT FOR USERNAME: " + username + " ===");

        Officer officer = officerRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("=== ERROR: USER NOT FOUND IN DB: " + username + " ===");
                    return new UsernameNotFoundException("Officer not found: " + username);
                });

        System.out.println("=== USER FOUND: " + officer.getUsername() + " | ACTIVE: " + officer.isActive() + " ===");

        return new User(
                officer.getUsername(),
                officer.getPassword(),
                officer.isActive(),
                true,
                true,
                true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + (officer.getRole() != null ? officer.getRole() : "OFFICER")))
        );
    }
}