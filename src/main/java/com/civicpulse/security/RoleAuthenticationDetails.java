package com.civicpulse.security;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class RoleAuthenticationDetails extends WebAuthenticationDetails {

    private final String role;

    public RoleAuthenticationDetails(
            HttpServletRequest request) {

        super(request);

        this.role = request.getParameter("role");
    }

    public String getRole() {
        return role;
    }
}