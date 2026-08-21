package com.civicpulse.repositories;

import com.civicpulse.entites.Complaint;
import com.civicpulse.entites.Officer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository
        extends JpaRepository<Complaint, Long> {


    /*
     * =========================================================
     * OFFICER
     * =========================================================
     */

    List<Complaint> findByAssignedOfficer(
            Officer officer
    );


    List<Complaint> findByAssignedOfficerAndStatus(
            Officer officer,
            String status
    );


    List<Complaint> findByAssignedOfficerAndPriority(
            Officer officer,
            String priority
    );


    /*
     * =========================================================
     * DEPARTMENT
     * =========================================================
     */

    List<Complaint> findByDepartment(
            String department
    );


    List<Complaint> findByDepartmentAndStatus(
            String department,
            String status
    );


    /*
     * =========================================================
     * COMPLAINT NUMBER
     * =========================================================
     */

    Optional<Complaint> findByComplaintNumber(
            String complaintNumber
    );


    /*
     * =========================================================
     * CITIZEN
     * =========================================================
     */

    List<Complaint>
    findByCitizenEmailOrderByCreatedAtDesc(
            String citizenEmail
    );


    /*
     * =========================================================
     * FETCH COMPLAINT + ATTACHMENTS BY ID
     * =========================================================
     */

    @Query("""
            SELECT DISTINCT c
            FROM Complaint c
            LEFT JOIN FETCH c.attachments
            WHERE c.id = :id
            """)
    Optional<Complaint> findByIdWithAttachments(
            @Param("id") Long id
    );


    /*
     * =========================================================
     * FETCH COMPLAINT + ATTACHMENTS BY COMPLAINT NUMBER
     * =========================================================
     */

    @Query("""
            SELECT DISTINCT c
            FROM Complaint c
            LEFT JOIN FETCH c.attachments
            WHERE c.complaintNumber = :complaintNumber
            """)
    Optional<Complaint>
    findByComplaintNumberWithAttachments(
            @Param("complaintNumber")
            String complaintNumber
    );
}

