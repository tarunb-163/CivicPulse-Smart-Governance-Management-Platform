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
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    // Lookups by Officer assignment
    List<Complaint> findByAssignedOfficer(Officer officer);

    List<Complaint> findByAssignedOfficerAndStatus(Officer officer, String status);

    List<Complaint> findByAssignedOfficerAndPriority(Officer officer, String priority);

    // Filter complaints by department
    List<Complaint> findByDepartment(String department);

    List<Complaint> findByDepartmentAndStatus(String department, String status);

    // Lookups by Complaint Number
    Optional<Complaint> findByComplaintNumber(String complaintNumber);

    /* =========================================================
       FETCH JOIN QUERIES (Prevents LazyInitializationException)
    ========================================================= */

    @Query("SELECT DISTINCT c FROM Complaint c LEFT JOIN FETCH c.attachments WHERE c.id = :id")
    Optional<Complaint> findByIdWithAttachments(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM Complaint c LEFT JOIN FETCH c.attachments WHERE c.complaintNumber = :complaintNumber")
    Optional<Complaint> findByComplaintNumberWithAttachments(@Param("complaintNumber") String complaintNumber);
}