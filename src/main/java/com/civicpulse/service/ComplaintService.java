package com.civicpulse.service;

import com.civicpulse.model.Complaint;
import com.civicpulse.repository.ComplaintRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    public ComplaintService(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    // Get all complaints
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    // Get complaint by ID
    public Complaint getComplaintById(Long id) {
        return complaintRepository.findById(id).orElse(null);
    }

    // Save complaint
    public Complaint saveComplaint(Complaint complaint) {
        return complaintRepository.save(complaint);
    }

    // Delete complaint
    public void deleteComplaint(Long id) {
        complaintRepository.deleteById(id);
    }

    // ================= DASHBOARD STATISTICS =================

    // Total complaints
    public long getTotalComplaints() {
        return complaintRepository.count();
    }

    // Pending complaints
    public long getPendingComplaints() {
        return countByStatus("Pending");
    }

    // Under Review complaints
    public long getUnderReviewComplaints() {
        return countByStatus("Under Review");
    }

    // Resolved complaints
    public long getResolvedComplaints() {
        return countByStatus("Resolved");
    }

    // Count complaints by status
    private long countByStatus(String status) {

        return complaintRepository.findAll()
                .stream()
                .filter(c ->
                        c.getStatus() != null &&
                                c.getStatus().equalsIgnoreCase(status)
                )
                .count();
    }

    // ================= RECENT COMPLAINTS =================

    // Get latest 3 complaints
    public List<Complaint> getRecentComplaints() {

        return complaintRepository.findAll()
                .stream()
                .sorted((c1, c2) ->
                        Long.compare(c2.getId(), c1.getId())
                )
                .limit(3)
                .toList();
    }
}