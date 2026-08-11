package com.civicpulse.controller;

import com.civicpulse.model.Complaint;
import com.civicpulse.service.ComplaintService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    private final ComplaintService complaintService;

    public AdminController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/complaints")
    public String complaints(Model model) {
        model.addAttribute("complaints", complaintService.getAllComplaints());
        return "admin/complaints";
    }

    @GetMapping("/admin/complaints/{id}")
    public String complaintDetails(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "complaint",
                complaintService.getComplaintById(id)
        );

        return "admin/complaint-details";
    }

    @PostMapping("/admin/complaints/{id}/status")
    public String updateComplaintStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Complaint complaint =
                complaintService.getComplaintById(id);

        if (complaint != null) {
            complaint.setStatus(status);
            complaintService.saveComplaint(complaint);
        }

        return "redirect:/admin/complaints/" + id;
    }

    @GetMapping("/admin/officers")
    public String officers() {
        return "admin/officers";
    }

    @GetMapping("/admin/citizens")
    public String citizens() {
        return "admin/citizens";
    }

    @GetMapping("/admin/reports")
    public String reports() {
        return "admin/reports";
    }

    @GetMapping("/admin/profile")
    public String profile() {
        return "admin/profile";
    }

    @GetMapping("/admin/settings")
    public String settings() {
        return "admin/settings";
    }
}