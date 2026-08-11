package com.civicpulse.repositories;

import com.civicpulse.entites.Officer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfficerRepository extends JpaRepository<Officer, Long> {

    Optional<Officer> findByUsername(String username);

    // Add this line so Spring Data JPA generates the SQL check automatically
    boolean existsByEmployeeId(String employeeId);
}