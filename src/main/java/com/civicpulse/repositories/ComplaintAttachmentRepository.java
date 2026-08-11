package com.civicpulse.repositories;

import com.civicpulse.entites.Complaint;
import com.civicpulse.entites.ComplaintAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintAttachmentRepository
        extends JpaRepository<ComplaintAttachment, Long> {

    List<ComplaintAttachment> findByComplaint(Complaint complaint);

}

