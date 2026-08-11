package com.civicpulse.services;

import com.civicpulse.dto.AttachmentDTO;
import com.civicpulse.dto.ComplaintDetailsDTO;
import com.civicpulse.entites.Complaint;
import com.civicpulse.entites.ComplaintAttachment;
import com.civicpulse.entites.Officer;
import com.civicpulse.repositories.ComplaintRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    public ComplaintService(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    /* =========================================================
       API & DTO RETRIEVAL METHODS
    ========================================================= */

    @Transactional(readOnly = true)
    public ComplaintDetailsDTO getComplaintDetailsById(String id) {
        Optional<Complaint> complaintOptional = findByStringIdentifier(id);

        if (complaintOptional.isEmpty()) {
            return null;
        }

        return mapToDTO(complaintOptional.get());
    }

    /* =========================================================
       ENTITY PERSISTENCE METHODS
    ========================================================= */

    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsForOfficer(Officer officer) {
        return complaintRepository.findByAssignedOfficer(officer);
    }

    @Transactional(readOnly = true)
    public Optional<Complaint> findById(Long id) {
        return complaintRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Complaint> findByComplaintNumber(String complaintNumber) {
        return complaintRepository.findByComplaintNumber(complaintNumber);
    }

    @Transactional
    public Complaint save(Complaint complaint) {
        return complaintRepository.save(complaint);
    }

    @Transactional
    public Complaint update(Complaint complaint) {
        return complaintRepository.save(complaint);
    }

    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsByStatus(Officer officer, String status) {
        return complaintRepository.findByAssignedOfficerAndStatus(officer, status);
    }

    @Transactional(readOnly = true)
    public List<Complaint> getComplaintsByPriority(Officer officer, String priority) {
        return complaintRepository.findByAssignedOfficerAndPriority(officer, priority);
    }

    /* =========================================================
       HELPER MAPPING METHODS
    ========================================================= */

    private Optional<Complaint> findByStringIdentifier(String identifier) {
        try {
            Long numericId = Long.parseLong(identifier);
            Optional<Complaint> byId = complaintRepository.findById(numericId);
            if (byId.isPresent()) {
                return byId;
            }
        } catch (NumberFormatException ignored) {
            // Identifier is a custom string (e.g. CP-2026-001)
        }

        return complaintRepository.findByComplaintNumber(identifier);
    }

    private ComplaintDetailsDTO mapToDTO(Complaint complaint) {
        ComplaintDetailsDTO dto = new ComplaintDetailsDTO();

        // Basic Info
        dto.setId(complaint.getComplaintNumber() != null ? complaint.getComplaintNumber() : String.valueOf(complaint.getId()));
        dto.setTitle(complaint.getTitle());
        dto.setDescription(complaint.getDescription());
        dto.setLocation(complaint.getLocation());
        dto.setDepartment(complaint.getDepartment());

        // Enum / String conversions
        dto.setCategory(complaint.getCategory() != null ? String.valueOf(complaint.getCategory()) : null);
        dto.setPriority(complaint.getPriority() != null ? String.valueOf(complaint.getPriority()) : null);
        dto.setStatus(complaint.getStatus() != null ? String.valueOf(complaint.getStatus()) : null);

        // Timestamps
        dto.setCreatedAt(complaint.getCreatedAt());
        dto.setAssignedAt(complaint.getAssignedAt());
        dto.setUpdatedAt(complaint.getUpdatedAt());
        dto.setResolvedAt(complaint.getResolvedAt());

        // Attachments
        if (complaint.getAttachments() != null && !complaint.getAttachments().isEmpty()) {
            List<AttachmentDTO> attachmentDTOs = complaint.getAttachments().stream()
                    .map(this::mapAttachmentToDTO)
                    .collect(Collectors.toList());
            dto.setAttachments(attachmentDTOs);
        } else {
            dto.setAttachments(Collections.emptyList());
        }

        return dto;
    }

    private AttachmentDTO mapAttachmentToDTO(ComplaintAttachment attachment) {
        AttachmentDTO dto = new AttachmentDTO();
        dto.setId(attachment.getId());
        dto.setFileName(attachment.getFileName());
        dto.setFileType(attachment.getFileType());
        dto.setFileSize(attachment.getFileSize());
        dto.setUploadedAt(attachment.getUploadedAt());

        if (attachment.getStoredFileName() != null) {
            dto.setFileUrl("/uploads/" + attachment.getStoredFileName());
        } else {
            dto.setFileUrl("#");
        }

        return dto;
    }
}