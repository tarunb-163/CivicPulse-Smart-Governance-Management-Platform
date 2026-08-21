package com.civicpulse.repositories;

import com.civicpulse.entites.Complaint;
import com.civicpulse.entites.ComplaintAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintAttachmentRepository
        extends JpaRepository<ComplaintAttachment, Long> {

    List<ComplaintAttachment>
    findByComplaint(Complaint complaint);
}