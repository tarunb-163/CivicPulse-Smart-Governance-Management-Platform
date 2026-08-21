package com.civicpulse.controller;

import com.civicpulse.entites.Citizen;
import com.civicpulse.entites.Complaint;
import com.civicpulse.entites.Officer;
import com.civicpulse.repositories.CitizenRepository;
import com.civicpulse.repositories.ComplaintRepository;
import com.civicpulse.repositories.OfficerRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
public class AdminController {


    // =========================================================
    // REPOSITORIES
    // =========================================================

    private final ComplaintRepository complaintRepository;

    private final OfficerRepository officerRepository;

    private final CitizenRepository citizenRepository;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public AdminController(
            ComplaintRepository complaintRepository,
            OfficerRepository officerRepository,
            CitizenRepository citizenRepository) {

        this.complaintRepository =
                complaintRepository;

        this.officerRepository =
                officerRepository;

        this.citizenRepository =
                citizenRepository;
    }


    // =========================================================
    // ADMIN DASHBOARD
    // =========================================================

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {


        System.out.println();
        System.out.println("=========================================");
        System.out.println("        CIVICPULSE ADMIN DASHBOARD");
        System.out.println("=========================================");


        // =====================================================
        // LOAD ALL COMPLAINTS
        // =====================================================

        List<Complaint> complaints =
                complaintRepository.findAll();


        // =====================================================
        // TOTAL COMPLAINTS
        // =====================================================

        long totalComplaints =
                complaints.size();


        // =====================================================
        // PENDING
        // =====================================================

        long pendingComplaints =
                complaints.stream()
                        .filter(this::isPending)
                        .count();


        // =====================================================
        // IN PROGRESS
        // =====================================================

        long inProgressComplaints =
                complaints.stream()
                        .filter(this::isInProgress)
                        .count();


        // =====================================================
        // RESOLVED
        // =====================================================

        long resolvedComplaints =
                complaints.stream()
                        .filter(this::isResolved)
                        .count();


        // =====================================================
        // REJECTED
        // =====================================================

        long rejectedComplaints =
                complaints.stream()
                        .filter(this::isRejected)
                        .count();


        // =====================================================
        // TOTAL OFFICERS
        // =====================================================

        long totalOfficers =
                officerRepository.count();


        // =====================================================
        // TOTAL CITIZENS
        // =====================================================

        long totalCitizens =
                citizenRepository.count();


        // =====================================================
        // PERCENTAGES
        // =====================================================

        double pendingPercentage = 0;

        double inProgressPercentage = 0;

        double resolvedPercentage = 0;

        double rejectedPercentage = 0;


        if (totalComplaints > 0) {

            pendingPercentage =
                    (pendingComplaints * 100.0)
                            / totalComplaints;

            inProgressPercentage =
                    (inProgressComplaints * 100.0)
                            / totalComplaints;

            resolvedPercentage =
                    (resolvedComplaints * 100.0)
                            / totalComplaints;

            rejectedPercentage =
                    (rejectedComplaints * 100.0)
                            / totalComplaints;
        }


        // =====================================================
        // RECENT COMPLAINTS
        // =====================================================

        List<Complaint> recentComplaints =
                complaints.stream()
                        .sorted(
                                Comparator.comparing(
                                        Complaint::getCreatedAt,
                                        Comparator.nullsLast(
                                                Comparator.reverseOrder()
                                        )
                                )
                        )
                        .limit(5)
                        .toList();


        // =====================================================
        // DEBUG
        // =====================================================

        System.out.println(
                "Total Complaints    : "
                        + totalComplaints
        );

        System.out.println(
                "Pending Complaints  : "
                        + pendingComplaints
        );

        System.out.println(
                "In Progress         : "
                        + inProgressComplaints
        );

        System.out.println(
                "Resolved Complaints : "
                        + resolvedComplaints
        );

        System.out.println(
                "Rejected Complaints : "
                        + rejectedComplaints
        );

        System.out.println(
                "Total Officers      : "
                        + totalOfficers
        );

        System.out.println(
                "Total Citizens      : "
                        + totalCitizens
        );

        System.out.println(
                "Recent Complaints   : "
                        + recentComplaints.size()
        );

        System.out.println(
                "========================================="
        );


        // =====================================================
        // SEND DATA TO THYMELEAF
        // =====================================================

        model.addAttribute(
                "totalComplaints",
                totalComplaints
        );

        model.addAttribute(
                "pendingComplaints",
                pendingComplaints
        );

        model.addAttribute(
                "inProgressComplaints",
                inProgressComplaints
        );

        model.addAttribute(
                "resolvedComplaints",
                resolvedComplaints
        );

        model.addAttribute(
                "rejectedComplaints",
                rejectedComplaints
        );

        model.addAttribute(
                "totalOfficers",
                totalOfficers
        );

        model.addAttribute(
                "totalCitizens",
                totalCitizens
        );

        model.addAttribute(
                "pendingPercentage",
                pendingPercentage
        );

        model.addAttribute(
                "inProgressPercentage",
                inProgressPercentage
        );

        model.addAttribute(
                "resolvedPercentage",
                resolvedPercentage
        );

        model.addAttribute(
                "rejectedPercentage",
                rejectedPercentage
        );

        model.addAttribute(
                "recentComplaints",
                recentComplaints
        );


        return "admin/dashboard";
    }


    // =========================================================
    // OFFICER MANAGEMENT
    // =========================================================

    @GetMapping("/admin/officers")
    public String officers(Model model) {


        List<Officer> officers =
                officerRepository.findAll();


        long totalOfficers =
                officers.size();


        long activeOfficers =
                officers.stream()
                        .filter(Officer::isActive)
                        .count();


        long inactiveOfficers =
                officers.stream()
                        .filter(officer -> !officer.isActive())
                        .count();


        model.addAttribute(
                "officers",
                officers
        );

        model.addAttribute(
                "totalOfficers",
                totalOfficers
        );

        model.addAttribute(
                "activeOfficers",
                activeOfficers
        );

        model.addAttribute(
                "inactiveOfficers",
                inactiveOfficers
        );


        return "admin/officers";
    }


    // =========================================================
    // ADMIN - ALL COMPLAINTS
    // =========================================================

    @GetMapping("/admin/complaints")
    public String complaints(Model model) {


        List<Complaint> complaints =
                complaintRepository.findAll();


        // =====================================================
        // COUNTS
        // =====================================================

        long totalComplaints =
                complaints.size();


        long pendingComplaints =
                complaints.stream()
                        .filter(this::isPending)
                        .count();


        long inProgressComplaints =
                complaints.stream()
                        .filter(this::isInProgress)
                        .count();


        long resolvedComplaints =
                complaints.stream()
                        .filter(this::isResolved)
                        .count();


        long rejectedComplaints =
                complaints.stream()
                        .filter(this::isRejected)
                        .count();


        // =====================================================
        // LOAD OFFICERS
        // =====================================================

        List<Officer> officers =
                officerRepository.findAll();


        // =====================================================
        // DEBUG
        // =====================================================

        System.out.println();
        System.out.println("=========================================");
        System.out.println("       CIVICPULSE COMPLAINT MANAGEMENT");
        System.out.println("=========================================");

        System.out.println(
                "Total Complaints : "
                        + totalComplaints
        );

        System.out.println(
                "Pending          : "
                        + pendingComplaints
        );

        System.out.println(
                "In Progress      : "
                        + inProgressComplaints
        );

        System.out.println(
                "Resolved         : "
                        + resolvedComplaints
        );

        System.out.println(
                "Rejected         : "
                        + rejectedComplaints
        );

        System.out.println(
                "========================================="
        );


        // =====================================================
        // SEND DATA TO THYMELEAF
        // =====================================================

        model.addAttribute(
                "complaints",
                complaints
        );

        model.addAttribute(
                "officers",
                officers
        );

        model.addAttribute(
                "totalComplaints",
                totalComplaints
        );

        model.addAttribute(
                "pendingComplaints",
                pendingComplaints
        );

        model.addAttribute(
                "inProgressComplaints",
                inProgressComplaints
        );

        model.addAttribute(
                "resolvedComplaints",
                resolvedComplaints
        );

        model.addAttribute(
                "rejectedComplaints",
                rejectedComplaints
        );


        return "admin/complaints";
    }


    // =========================================================
    // ADMIN - COMPLAINT DETAILS
    // =========================================================

    @GetMapping("/admin/complaints/{id}")
    public String complaintDetails(
            @PathVariable Long id,
            Model model) {


        Optional<Complaint> optionalComplaint =
                complaintRepository.findByIdWithAttachments(id);


        if (optionalComplaint.isEmpty()) {

            model.addAttribute(
                    "complaint",
                    null
            );

            return "admin/complaint-details";
        }


        Complaint complaint =
                optionalComplaint.get();


        // =====================================================
        // LOAD OFFICERS
        // =====================================================

        List<Officer> officers =
                officerRepository.findAll();


        model.addAttribute(
                "complaint",
                complaint
        );

        model.addAttribute(
                "officers",
                officers
        );


        return "admin/complaint-details";
    }


    // =========================================================
    // ADMIN - ASSIGN COMPLAINT TO OFFICER
    // =========================================================
    //
    // ADMIN CAN ONLY:
    //
    //     Assign complaint -> Officer
    //
    // ADMIN CANNOT:
    //
    //     Change status
    //     Resolve complaint
    //     Reject complaint
    //     Mark complaint as in progress
    //
    // =========================================================

    @PostMapping("/admin/complaints/{id}/assign")
    public String assignComplaint(
            @PathVariable Long id,
            @RequestParam Long officerId,
            RedirectAttributes redirectAttributes) {


        // =====================================================
        // FIND COMPLAINT
        // =====================================================

        Optional<Complaint> complaintOptional =
                complaintRepository.findById(id);


        if (complaintOptional.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Complaint not found."
            );

            return "redirect:/admin/complaints";
        }


        // =====================================================
        // FIND OFFICER
        // =====================================================

        Optional<Officer> officerOptional =
                officerRepository.findById(officerId);


        if (officerOptional.isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Officer not found."
            );

            return "redirect:/admin/complaints/" + id;
        }


        Complaint complaint =
                complaintOptional.get();

        Officer officer =
                officerOptional.get();


        // =====================================================
        // ASSIGN ONLY
        // =====================================================

        complaint.setAssignedOfficer(officer);

        complaint.setAssignedAt(
                LocalDateTime.now()
        );


        // =====================================================
        // IMPORTANT
        // =====================================================
        //
        // DO NOT CHANGE STATUS HERE.
        //
        // The officer is responsible for:
        //
        // PENDING
        // IN_PROGRESS
        // RESOLVED
        // REJECTED
        //
        // =====================================================

        complaintRepository.save(complaint);


        redirectAttributes.addFlashAttribute(
                "success",
                "Complaint assigned successfully to officer."
        );


        return "redirect:/admin/complaints/" + id;
    }


    // =========================================================
    // ADMIN - CITIZENS
    // =========================================================

    @GetMapping("/admin/citizens")
    public String citizens(Model model) {


        System.out.println();
        System.out.println("=========================================");
        System.out.println("        CIVICPULSE CITIZENS MANAGEMENT");
        System.out.println("=========================================");


        // =====================================================
        // LOAD CITIZENS
        // =====================================================

        List<Citizen> citizens =
                citizenRepository.findAll();


        // =====================================================
        // TOTAL CITIZENS
        // =====================================================

        long totalCitizens =
                citizens.size();


        // =====================================================
        // DEBUG
        // =====================================================

        System.out.println(
                "Registered Citizens : "
                        + totalCitizens
        );

        System.out.println(
                "========================================="
        );


        // =====================================================
        // SEND DATA TO THYMELEAF
        // =====================================================

        model.addAttribute(
                "citizens",
                citizens
        );

        model.addAttribute(
                "totalCitizens",
                totalCitizens
        );


        return "admin/citizens";
    }


    // =========================================================
    // ADMIN - PROFILE
    // =========================================================

    @GetMapping("/admin/profile")
    public String profile(Model model) {


        // =====================================================
        // STATIC ADMIN PROFILE
        // =====================================================
        //
        // The current profile page is a system-admin profile.
        // No database update is required.
        //
        // =====================================================

        model.addAttribute(
                "adminName",
                "Administrator"
        );

        model.addAttribute(
                "adminRole",
                "System Admin"
        );

        model.addAttribute(
                "accountType",
                "Administrator"
        );


        return "admin/profile";
    }


    // =========================================================
    // STATUS NORMALIZATION
    // =========================================================

    private String normalizeStatus(
            Complaint complaint) {


        if (complaint == null) {

            return "";
        }


        if (complaint.getStatus() == null) {

            return "";
        }


        return complaint.getStatus()
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace("-", "_")
                .replace(" ", "_");
    }


    // =========================================================
    // CHECK PENDING
    // =========================================================

    private boolean isPending(
            Complaint complaint) {


        String status =
                normalizeStatus(complaint);


        return status.equals("PENDING");
    }


    // =========================================================
    // CHECK IN PROGRESS
    // =========================================================

    private boolean isInProgress(
            Complaint complaint) {


        String status =
                normalizeStatus(complaint);


        return status.equals("IN_PROGRESS")
                || status.equals("UNDER_REVIEW")
                || status.equals("IN_REVIEW");
    }


    // =========================================================
    // CHECK RESOLVED
    // =========================================================

    private boolean isResolved(
            Complaint complaint) {


        String status =
                normalizeStatus(complaint);


        return status.equals("RESOLVED")
                || status.equals("CLOSED");
    }


    // =========================================================
    // CHECK REJECTED
    // =========================================================

    private boolean isRejected(
            Complaint complaint) {


        String status =
                normalizeStatus(complaint);


        return status.equals("REJECTED")
                || status.equals("DECLINED");
    }

}

