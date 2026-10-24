package com.civicpulse.repository;

import com.civicpulse.entity.Upvote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UpvoteRepository extends JpaRepository<Upvote, Long> {
    Optional<Upvote> findByComplaintIdAndUserId(Long complaintId, Long userId);
    boolean existsByComplaintIdAndUserId(Long complaintId, Long userId);
    long countByComplaintId(Long complaintId);
    List<Upvote> findByComplaintId(Long complaintId);
}
