package com.civicpulse.controller;

import com.civicpulse.entites.Complaint;
import com.civicpulse.entites.Officer;
import com.civicpulse.services.OfficerService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;


@Controller
public class OfficerController {

    private final OfficerService officerService;


    public OfficerController(
            OfficerService officerService) {

        this.officerService =
                officerService;
    }


    // =========================================================
    // HOME
    // =========================================================

    @GetMapping("/")
    public String home() {

        /*
         * All users use the same login page.
         *
         * templates/officer/login.html
         */

        return "redirect:/officer/login";
    }


    // =========================================================
    // COMMON LOGIN PAGE
    // =========================================================

    @GetMapping("/officer/login")
    public String officerLogin() {

        /*
         * This renders:
         *
         * templates/officer/login.html
         *
         * Citizen, Officer and Admin all use
         * this same page.
         */

        return "officer/login";
    }


    // =========================================================
    // OFFICER DASHBOARD
    // =========================================================

    @GetMapping("/officer/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model) {

        String username =
                authentication.getName();


        Officer officer =
                officerService
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Officer not found: "
                                                + username
                                )
                        );


        model.addAttribute(
                "officer",
                officer
        );


        return "officer/dashboard";
    }


    // =========================================================
    // OFFICER COMPLAINTS
    // =========================================================

    @GetMapping("/officer/complaints")
    public String complaints(
            Authentication authentication,
            Model model) {

        String username =
                authentication.getName();


        List<Complaint> complaints =
                officerService
                        .getAssignedComplaints(
                                username
                        );


        model.addAttribute(
                "complaints",
                complaints
        );


        return "officer/complaints";
    }


    // =========================================================
    // COMPLAINT DETAILS
    // =========================================================

    @GetMapping("/officer/complaint-details")
    public String complaintDetails() {

        return "officer/complaint-details";
    }


    // =========================================================
    // UPDATE COMPLAINT PAGE
    // =========================================================

    @GetMapping("/officer/update-complaint")
    public String updateComplaint() {

        return "officer/update-complaint";
    }


    // =========================================================
    // OFFICER PROFILE - GET
    // =========================================================

    @GetMapping("/officer/profile")
    public String profile(
            Authentication authentication,
            Model model) {

        /*
         * Get currently logged-in officer.
         */

        String username =
                authentication.getName();


        /*
         * Load officer from database.
         */

        Officer officer =
                officerService
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Officer not found: "
                                                + username
                                )
                        );


        /*
         * Add officer to model.
         */

        model.addAttribute(
                "officer",
                officer
        );


        /*
         * Generate profile initials.
         */

        String initials =
                getInitials(
                        officer.getFullName()
                );


        model.addAttribute(
                "profileInitials",
                initials
        );


        return "officer/profile";
    }


    // =========================================================
    // OFFICER PROFILE - UPDATE
    // =========================================================

    @PostMapping("/officer/profile")
    public String updateProfile(
            Authentication authentication,
            Officer formOfficer,
            RedirectAttributes redirectAttributes) {

        /*
         * Get currently logged-in officer.
         */

        String username =
                authentication.getName();


        /*
         * Find the real officer.
         */

        Officer officer =
                officerService
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Officer not found: "
                                                + username
                                )
                        );


        /*
         * Update ONLY editable fields.
         *
         * Do NOT update:
         *
         * id
         * username
         * password
         * employeeId
         * role
         * jurisdiction
         * joinedDate
         * active
         */

        officer.setFullName(
                formOfficer.getFullName()
        );

        officer.setEmail(
                formOfficer.getEmail()
        );

        officer.setPhone(
                formOfficer.getPhone()
        );

        officer.setDesignation(
                formOfficer.getDesignation()
        );

        officer.setDepartment(
                formOfficer.getDepartment()
        );

        officer.setAssignedArea(
                formOfficer.getAssignedArea()
        );

        officer.setOfficeLocation(
                formOfficer.getOfficeLocation()
        );

        officer.setAddress(
                formOfficer.getAddress()
        );


        /*
         * Save officer.
         */

        officerService.update(
                officer
        );


        /*
         * Success message.
         */

        redirectAttributes.addFlashAttribute(
                "profileSuccess",
                "Your profile information has been successfully updated."
        );


        /*
         * Redirect after POST.
         */

        return "redirect:/officer/profile";
    }


    // =========================================================
    // CHANGE PASSWORD PAGE
    // =========================================================

    @GetMapping("/officer/change-password")
    public String changePassword() {

        return "officer/change-password";
    }


    // =========================================================
    // UPDATE COMPLAINT STATUS API
    // =========================================================

    @PostMapping(
            "/officer/api/complaints/{id}/status"
    )
    public ResponseEntity<?> updateComplaintStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody,
            Authentication authentication) {


        String status =
                requestBody.get("status");


        String remarks =
                requestBody.get("remarks");


        String username =
                authentication.getName();


        boolean updated =
                officerService.updateComplaintStatus(
                        id,
                        status,
                        remarks,
                        username
                );


        if (updated) {

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Status updated successfully"
                    )
            );
        }


        return ResponseEntity
                .badRequest()
                .body(
                        Map.of(
                                "message",
                                "Failed to update complaint status"
                        )
                );
    }


    // =========================================================
    // PROFILE INITIALS
    // =========================================================

    private String getInitials(
            String fullName) {

        if (fullName == null ||
                fullName.trim().isEmpty()) {

            return "O";
        }


        String[] parts =
                fullName
                        .trim()
                        .split("\\s+");


        if (parts.length == 1) {

            return parts[0]
                    .substring(0, 1)
                    .toUpperCase();
        }


        String first =
                parts[0]
                        .substring(0, 1)
                        .toUpperCase();


        String last =
                parts[parts.length - 1]
                        .substring(0, 1)
                        .toUpperCase();


        return first + last;
    }
}
