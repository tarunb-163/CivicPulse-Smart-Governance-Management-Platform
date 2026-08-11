package com.civicpulse.services;

import com.civicpulse.entites.Complaint;
import com.civicpulse.entites.ComplaintAttachment;
import com.civicpulse.repositories.ComplaintAttachmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComplaintAttachmentService {

    private final ComplaintAttachmentRepository attachmentRepository;

    public ComplaintAttachmentService(
            ComplaintAttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    public List<ComplaintAttachment> getAttachmentsForComplaint(
            Complaint complaint) {

        return attachmentRepository.findByComplaint(complaint);
    }

    public ComplaintAttachment save(ComplaintAttachment attachment) {
        return attachmentRepository.save(attachment);
    }

    public void delete(Long id) {
        attachmentRepository.deleteById(id);
    }
}

