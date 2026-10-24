package com.civicpulse.service.impl;

import com.civicpulse.entity.DevOutbox;
import com.civicpulse.repository.DevOutboxRepository;
import com.civicpulse.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final DevOutboxRepository devOutboxRepository;
    private final org.springframework.beans.factory.ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    @Async("taskExecutor")
    @Override
    public void sendEmail(String to, String subject, String body, String link) {
        log.info("Sending email to {}: [{}] (link: {})", to, subject, link);

        // Always save to dev_outbox so verification/reset links are verifiable and viewable in admin portal
        DevOutbox outboxEntry = DevOutbox.builder()
                .recipientEmail(to)
                .subject(subject)
                .bodyText(body)
                .link(link)
                .build();
        devOutboxRepository.save(outboxEntry);

        // If real SMTP host is provided, attempt live send
        if (StringUtils.hasText(mailHost)) {
            try {
                JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
                if (mailSender != null) {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(to);
                    message.setSubject(subject);
                    message.setText(body + (link != null ? "\n\nLink: " + link : ""));
                    mailSender.send(message);
                    log.info("Live email successfully sent to {}", to);
                }
            } catch (Exception e) {
                log.warn("SMTP send failed for {}: {}. Logged in dev_outbox.", to, e.getMessage());
            }
        }
    }

    @Override
    public void sendRegistrationVerification(String to, String name, String token) {
        String link = publicBaseUrl + "/api/v1/auth/verify-email?token=" + token;
        String subject = "Verify your CivicPulse Citizen Account";
        String body = "Hello " + name + ",\n\n"
                + "Thank you for registering on CivicPulse. Please verify your email by clicking the link below:\n\n"
                + link + "\n\n"
                + "If you did not request this, please ignore this email.";
        sendEmail(to, subject, body, link);
    }

    @Override
    public void sendPasswordReset(String to, String name, String token) {
        String link = publicBaseUrl + "/login.html?token=" + token + "#reset";
        String subject = "Reset your CivicPulse Password";
        String body = "Hello " + name + ",\n\n"
                + "We received a request to reset your password. Use the link below to set a new password:\n\n"
                + link + "\n\n"
                + "This token expires in 1 hour. If you did not request this, please ignore this email.";
        sendEmail(to, subject, body, link);
    }

    @Override
    public void sendStatusUpdate(String to, String name, String complaintCode, String newStatus, String remark) {
        String link = publicBaseUrl + "/track.html?code=" + complaintCode;
        String subject = "Complaint Status Update: " + complaintCode + " is now " + newStatus;
        String body = "Hello " + name + ",\n\n"
                + "Your complaint " + complaintCode + " status has changed to: " + newStatus + ".\n"
                + "Officer Remark: " + (remark != null ? remark : "None") + "\n\n"
                + "Track live progress: " + link;
        sendEmail(to, subject, body, link);
    }
}
