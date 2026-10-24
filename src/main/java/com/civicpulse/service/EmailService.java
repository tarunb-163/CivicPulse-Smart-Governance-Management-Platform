package com.civicpulse.service;

public interface EmailService {
    void sendEmail(String to, String subject, String body, String link);
    void sendRegistrationVerification(String to, String name, String token);
    void sendPasswordReset(String to, String name, String token);
    void sendStatusUpdate(String to, String name, String complaintCode, String newStatus, String remark);
}
