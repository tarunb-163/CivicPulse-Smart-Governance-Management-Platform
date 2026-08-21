package com.civicpulse.repositories;

import com.civicpulse.entites.Officer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfficerRepository
        extends JpaRepository<Officer, Long> {


    // =========================================================
    // LOGIN
    // =========================================================

    Optional<Officer> findByUsername(
            String username
    );


    // =========================================================
    // EMPLOYEE ID
    // =========================================================

    boolean existsByEmployeeId(
            String employeeId
    );


    // =========================================================
    // EMAIL
    // =========================================================

    Optional<Officer> findByEmail(
            String email
    );


    // =========================================================
    // ACTIVE / INACTIVE
    // =========================================================

    long countByActiveTrue();


    long countByActiveFalse();


    // =========================================================
    // ACTIVE OFFICERS
    // =========================================================

    List<Officer> findByActiveTrue();


    // =========================================================
    // INACTIVE OFFICERS
    // =========================================================

    List<Officer> findByActiveFalse();


    // =========================================================
    // DEPARTMENT
    // =========================================================

    List<Officer> findByDepartment(
            String department
    );


    // =========================================================
    // SEARCH
    // =========================================================

    List<Officer>
    findByFullNameContainingIgnoreCaseOrDepartmentContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullName,
            String department,
            String email
    );

}

