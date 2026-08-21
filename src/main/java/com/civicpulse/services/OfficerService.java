package com.civicpulse.services;

import com.civicpulse.entites.Complaint;
import com.civicpulse.entites.Officer;
import com.civicpulse.repositories.ComplaintRepository;
import com.civicpulse.repositories.OfficerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OfficerService {

    private final OfficerRepository officerRepository;
    private final ComplaintRepository complaintRepository;

    public OfficerService(
            OfficerRepository officerRepository,
            ComplaintRepository complaintRepository) {

        this.officerRepository = officerRepository;
        this.complaintRepository = complaintRepository;
    }

    public Optional<Officer> findByUsername(String username) {
        return officerRepository.findByUsername(username);
    }

    public Optional<Officer> findById(Long id) {
        return officerRepository.findById(id);
    }

    public Officer save(Officer officer) {
        return officerRepository.save(officer);
    }

    public Officer update(Officer officer) {
        return officerRepository.save(officer);
    }

    public List<Complaint> getAssignedComplaints(String username) {

        Officer officer = officerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Officer not found"));

        return complaintRepository.findByAssignedOfficer(officer);
    }

    @Transactional
    public boolean updateComplaintStatus(Long complaintId, String newStatus, String remarks, String username) {
        Optional<Complaint> optionalComplaint = complaintRepository.findById(complaintId);

        if (optionalComplaint.isPresent()) {
            Complaint complaint = optionalComplaint.get();

            // Check if the complaint is assigned to this officer
            if (complaint.getAssignedOfficer() != null &&
                    username.equalsIgnoreCase(complaint.getAssignedOfficer().getUsername())) {

                complaint.setStatus(newStatus);
                complaint.setRemarks(remarks);
                complaint.setUpdatedAt(LocalDateTime.now());

                // Update timestamps based on the status selected
                if ("IN_PROGRESS".equalsIgnoreCase(newStatus)) {
                    complaint.setInProgressAt(LocalDateTime.now());
                } else if ("RESOLVED".equalsIgnoreCase(newStatus)) {
                    complaint.setResolvedAt(LocalDateTime.now());
                }

                complaintRepository.save(complaint);
                return true;
            }
        }
        return false;
    }
}