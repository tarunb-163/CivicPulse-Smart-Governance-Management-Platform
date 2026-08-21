package com.civicpulse.security;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class RoleAuthenticationDetailsSource
        implements AuthenticationDetailsSource<
        HttpServletRequest,
        WebAuthenticationDetails> {

    @Override
    public WebAuthenticationDetails buildDetails(
            HttpServletRequest context) {

        return new RoleAuthenticationDetails(context);
    }
}