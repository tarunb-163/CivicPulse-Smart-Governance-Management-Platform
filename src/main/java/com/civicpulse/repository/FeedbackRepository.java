package com.civicpulse.repository;

import com.civicpulse.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Optional<Feedback> findByComplaintId(Long complaintId);
    boolean existsByComplaintId(Long complaintId);

    @Query("SELECT AVG(f.rating) FROM Feedback f JOIN f.complaint c WHERE c.department.id = :departmentId")
    Double findAverageRatingByDepartment(@Param("departmentId") Long departmentId);

    @Query("SELECT AVG(f.rating) FROM Feedback f")
    Double findOverallAverageRating();
}
