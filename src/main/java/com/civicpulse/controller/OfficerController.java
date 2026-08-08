package com.civicpulse.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OfficerController {

    @GetMapping("/")
    public String home() {
        return "redirect:/officer/login";
    }

    @GetMapping("/officer/login")
    public String officerLogin() {
        return "officer/login";
    }

    @GetMapping("/officer/dashboard")
    public String dashboard() {
        return "officer/dashboard";
    }

    @GetMapping("/officer/complaints")
    public String complaints() {
        return "officer/complaints";
    }

    @GetMapping("/officer/complaint-details")
    public String complaintDetails() {
        return "officer/complaint-details";
    }

    @GetMapping("/officer/update-complaint")
    public String updateComplaint() {
        return "officer/update-complaint";
    }

    @GetMapping("/officer/profile")
    public String profile() {
        return "officer/profile";
    }

    @GetMapping("/officer/change-password")
    public String changePassword() {
        return "officer/change-password";
    }
}