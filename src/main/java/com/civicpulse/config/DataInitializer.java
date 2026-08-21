package com.civicpulse.config;

import com.civicpulse.entites.Officer;
import com.civicpulse.repositories.OfficerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createDefaultOfficer(
            OfficerRepository officerRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // Check by both username and employeeId to prevent duplication crashes
            if (officerRepository.findByUsername("officer1").isEmpty()
                    && !officerRepository.existsByEmployeeId("EMP001")) {

                Officer officer = new Officer();

                officer.setUsername("officer1");
                officer.setPassword(
                        passwordEncoder.encode("password123")
                );
                officer.setFullName("Officer One");
                officer.setEmployeeId("EMP001");
                officer.setEmail("officer1@civicpulse.com");
                officer.setDesignation("Electricity Officer");
                officer.setDepartment("Electricity");
                officer.setRole("OFFICER");
                officer.setJoinedDate(LocalDate.now());
                officer.setActive(true);

                officerRepository.save(officer);

                System.out.println("Default officer created: officer1");
            }
        };
    }
}