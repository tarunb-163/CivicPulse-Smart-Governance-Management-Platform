package com.civicpulse.controller;

import com.civicpulse.entites.Citizen;
import com.civicpulse.repositories.CitizenRepository;
import com.civicpulse.services.ComplaintService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class CitizenController {

    private final CitizenRepository citizenRepository;

    private final ComplaintService complaintService;


    public CitizenController(
            CitizenRepository citizenRepository,
            ComplaintService complaintService) {

        this.citizenRepository =
                citizenRepository;

        this.complaintService =
                complaintService;
    }


    // =========================================================
    // CITIZEN DASHBOARD
    // =========================================================

    @GetMapping("/citizen/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model) {

        String username =
                authentication.getName();


        Citizen citizen =
                citizenRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Citizen not found: "
                                                + username
                                )
                        );


        /*
         * Get complaints submitted by citizen.
         */

        var complaints =
                complaintService
                        .getComplaintsForCitizen(
                                citizen.getEmail()
                        );


        /*
         * Citizen information.
         */

        model.addAttribute(
                "citizen",
                citizen
        );


        /*
         * Complaints.
         */

        model.addAttribute(
                "complaints",
                complaints
        );


        // =====================================================
        // STATISTICS
        // =====================================================

        long totalComplaints =
                complaints.size();


        long pendingComplaints =
                complaints.stream()
                        .filter(c ->
                                "PENDING".equalsIgnoreCase(
                                        c.getStatus()
                                )
                        )
                        .count();


        long inProgressComplaints =
                complaints.stream()
                        .filter(c ->
                                "IN_PROGRESS".equalsIgnoreCase(
                                        c.getStatus()
                                )
                        )
                        .count();


        long resolvedComplaints =
                complaints.stream()
                        .filter(c ->
                                "RESOLVED".equalsIgnoreCase(
                                        c.getStatus()
                                )
                        )
                        .count();


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


        return "citizen/dashboard";
    }


    // =========================================================
    // MY COMPLAINTS
    // =========================================================

    @GetMapping("/citizen/complaints")
    public String complaints(
            Authentication authentication,
            Model model) {

        String username =
                authentication.getName();


        Citizen citizen =
                citizenRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Citizen not found: "
                                                + username
                                )
                        );


        var complaints =
                complaintService
                        .getComplaintsForCitizen(
                                citizen.getEmail()
                        );


        model.addAttribute(
                "citizen",
                citizen
        );


        model.addAttribute(
                "complaints",
                complaints
        );


        return "citizen/complaints";
    }


    // =========================================================
    // TRACK COMPLAINT
    // =========================================================

    @GetMapping("/citizen/track")
    public String trackComplaint(
            Authentication authentication,
            Model model) {

        String username =
                authentication.getName();


        Citizen citizen =
                citizenRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Citizen not found: "
                                                + username
                                )
                        );


        model.addAttribute(
                "citizen",
                citizen
        );


        return "citizen/track";
    }


    // =========================================================
    // NOTIFICATIONS
    // =========================================================

    @GetMapping("/citizen/notifications")
    public String notifications(
            Authentication authentication,
            Model model) {

        String username =
                authentication.getName();


        Citizen citizen =
                citizenRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Citizen not found: "
                                                + username
                                )
                        );


        model.addAttribute(
                "citizen",
                citizen
        );


        return "citizen/notifications";
    }


    // =========================================================
    // PROFILE - GET
    // =========================================================

    @GetMapping("/citizen/profile")
    public String profile(
            Authentication authentication,
            Model model) {

        String username =
                authentication.getName();


        Citizen citizen =
                citizenRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Citizen not found: "
                                                + username
                                )
                        );


        model.addAttribute(
                "citizen",
                citizen
        );


        return "citizen/profile";
    }


    // =========================================================
    // PROFILE - POST
    // =========================================================
    //
    // POST /citizen/profile
    //
    // Updates the currently logged-in citizen.
    //
    // =========================================================

    @PostMapping("/citizen/profile")
    public String updateProfile(
            Authentication authentication,
            Model model,

            @RequestParam("fullName")
            String fullName,

            @RequestParam("email")
            String email,

            @RequestParam("phone")
            String phone,

            @RequestParam(
                    value = "address",
                    required = false
            )
            String address,

            RedirectAttributes redirectAttributes) {


        /*
         * NEVER get username from the HTML form.
         *
         * Always get it from Spring Security.
         */

        String username =
                authentication.getName();


        /*
         * Find logged-in citizen.
         */

        Citizen citizen =
                citizenRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Citizen not found: "
                                                + username
                                )
                        );


        /*
         * Update editable fields.
         */

        citizen.setFullName(
                fullName
        );

        citizen.setEmail(
                email
        );

        citizen.setPhone(
                phone
        );

        citizen.setAddress(
                address
        );


        /*
         * Save changes.
         */

        citizenRepository.save(
                citizen
        );


        /*
         * Success message.
         */

        redirectAttributes.addFlashAttribute(
                "profileSuccess",
                "Profile updated successfully!"
        );


        /*
         * Redirect after POST.
         *
         * Prevents duplicate submission
         * when browser is refreshed.
         */

        return "redirect:/citizen/profile";
    }
}

