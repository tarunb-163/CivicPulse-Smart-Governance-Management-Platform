package com.civicpulse.repositories;

import com.civicpulse.entites.Citizen;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CitizenRepository
        extends JpaRepository<Citizen, Long> {

    Optional<Citizen> findByUsername(
            String username
    );


    Optional<Citizen> findByUsernameAndRole(
            String username,
            String role
    );


    boolean existsByCitizenId(
            String citizenId
    );
}