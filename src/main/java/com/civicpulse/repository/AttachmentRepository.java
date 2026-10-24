package com.civicpulse.repository;

import com.civicpulse.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByComplaintId(Long complaintId);
    Optional<Attachment> findByStoredName(String storedName);

    // Exact and perceptual image duplicate detection
    List<Attachment> findBySha256(String sha256);
    List<Attachment> findByPhash(String phash);

    @Query("SELECT a FROM Attachment a JOIN a.complaint c WHERE a.sha256 = :sha256 AND c.status NOT IN ('RESOLVED', 'CLOSED', 'REJECTED')")
    List<Attachment> findOpenAttachmentsBySha256(@Param("sha256") String sha256);

    @Query("SELECT a FROM Attachment a JOIN a.complaint c WHERE a.phash IS NOT NULL AND c.status NOT IN ('RESOLVED', 'CLOSED', 'REJECTED')")
    List<Attachment> findAllOpenAttachmentsWithPhash();
}
