package com.civicpulse.repository;

import com.civicpulse.entity.ComplaintFollower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintFollowerRepository extends JpaRepository<ComplaintFollower, Long> {
    Optional<ComplaintFollower> findByComplaintIdAndUserId(Long complaintId, Long userId);
    boolean existsByComplaintIdAndUserId(Long complaintId, Long userId);
    List<ComplaintFollower> findByComplaintId(Long complaintId);
}
