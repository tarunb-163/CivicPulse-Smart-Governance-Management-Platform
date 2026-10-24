package com.civicpulse.service.impl;

import com.civicpulse.dto.request.BotMessageRequest;
import com.civicpulse.dto.response.BotMessageResponse;
import com.civicpulse.entity.Complaint;
import com.civicpulse.entity.Department;
import com.civicpulse.repository.ComplaintRepository;
import com.civicpulse.repository.DepartmentRepository;
import com.civicpulse.service.BotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BotServiceImpl implements BotService {

    private final ComplaintRepository complaintRepository;
    private final DepartmentRepository departmentRepository;

    private static final Pattern COMPLAINT_CODE_PATTERN = Pattern.compile("CP-\\d{4}-\\d{6}", Pattern.CASE_INSENSITIVE);

    @Override
    @Transactional(readOnly = true)
    public BotMessageResponse processMessage(BotMessageRequest request) {
        String msg = request.getMessage().trim();
        String lower = msg.toLowerCase();

        // 1. Check for Complaint Code match (e.g. CP-2026-000123)
        Matcher matcher = COMPLAINT_CODE_PATTERN.matcher(msg);
        if (matcher.find()) {
            String code = matcher.group().toUpperCase();
            Optional<Complaint> complaintOpt = complaintRepository.findByPublicCode(code);
            if (complaintOpt.isPresent()) {
                Complaint c = complaintOpt.get();
                String deptName = c.getDepartment() != null ? c.getDepartment().getName() : "General";
                String reply = "Complaint **" + code + "** status is **" + c.getStatus().name() + "**.\n\n"
                        + "• **Title:** " + c.getTitle() + "\n"
                        + "• **Department:** " + deptName + "\n"
                        + "• **Priority Score:** " + c.getPriorityScore() + "\n"
                        + "• **Upvotes:** " + c.getUpvoteCount() + "\n\n"
                        + "You can track full updates and progress timeline directly at `/track.html?code=" + code + "`.";

                return BotMessageResponse.builder()
                        .intent("TRACK_COMPLAINT")
                        .reply(reply)
                        .quickReplies(Arrays.asList("Track another complaint", "How to file a complaint", "Working hours"))
                        .metadata(Map.of("code", code, "status", c.getStatus().name()))
                        .build();
            } else {
                return BotMessageResponse.builder()
                        .intent("TRACK_NOT_FOUND")
                        .reply("I couldn't find any complaint with code **" + code + "**. Please double check the 6-digit sequence or your SMS/receipt confirmation.")
                        .quickReplies(Arrays.asList("How to file a complaint", "Contact department", "FAQs"))
                        .build();
            }
        }

        // 2. Intent: How to file complaint
        if (lower.contains("file") || lower.contains("how to") || lower.contains("submit") || lower.contains("raise") || lower.contains("lodge") || lower.contains("register complaint")) {
            String reply = "Filing a civic grievance on CivicPulse takes under 90 seconds in 4 simple steps:\n\n"
                    + "1. **Details:** Enter a title and description. Our system auto-suggests the category.\n"
                    + "2. **Location:** Drop a pin on the map or use your current GPS location.\n"
                    + "3. **Evidence:** Attach up to 5 photos or PDFs of the issue.\n"
                    + "4. **Review:** Choose public or anonymous mode and submit to receive your official tracking code!\n\n"
                    + "Click the **'Raise Complaint'** button in your dashboard to start.";

            return BotMessageResponse.builder()
                    .intent("HOW_TO_FILE")
                    .reply(reply)
                    .quickReplies(Arrays.asList("Start filing complaint", "What categories are supported?", "Track existing complaint"))
                    .build();
        }

        // 3. Intent: Working hours & contact
        if (lower.contains("hours") || lower.contains("timing") || lower.contains("office") || lower.contains("open") || lower.contains("when")) {
            String reply = "Administrative office hours for field operations are **Monday to Saturday, 9:00 AM – 5:30 PM**.\n\n"
                    + "The **CivicPulse Web Portal & CivicBot** are active **24/7/365** — complaints lodged after hours are queued and assigned automatically at the next shift.";

            return BotMessageResponse.builder()
                    .intent("WORKING_HOURS")
                    .reply(reply)
                    .quickReplies(Arrays.asList("List departments", "How long does resolution take?", "File a complaint"))
                    .build();
        }

        // 4. Intent: Departments
        if (lower.contains("department") || lower.contains("dept") || lower.contains("who handles") || lower.contains("contact")) {
            List<Department> depts = departmentRepository.findByActiveTrue();
            String deptList = depts.stream()
                    .map(d -> "• **" + d.getName() + "** (" + d.getCode() + ") — SLA: " + d.getDefaultSlaHours() + "h")
                    .collect(Collectors.joining("\n"));

            String reply = "CivicPulse integrates with " + depts.size() + " active municipal departments:\n\n"
                    + deptList + "\n\n"
                    + "Our rule engine automatically routes your complaint to the correct team based on your description.";

            return BotMessageResponse.builder()
                    .intent("DEPARTMENTS_LIST")
                    .reply(reply)
                    .quickReplies(Arrays.asList("How does auto-routing work?", "Track a complaint", "Working hours"))
                    .build();
        }

        // 5. Intent: SLA / Timelines
        if (lower.contains("sla") || lower.contains("time") || lower.contains("how long") || lower.contains("days") || lower.contains("deadline")) {
            String reply = "Each complaint is governed by an automated Service Level Agreement (SLA):\n\n"
                    + "• **Critical / Emergency (e.g. Broken live wires, open manholes):** 12 – 24 hours\n"
                    + "• **Standard (Potholes, garbage, water leaks):** 24 – 48 hours\n"
                    + "• **General civic works:** 72 – 96 hours\n\n"
                    + "If an SLA is breached, CivicPulse auto-escalates the complaint to the Department Head, and subsequently to Municipal Administrators.";

            return BotMessageResponse.builder()
                    .intent("SLA_INFO")
                    .reply(reply)
                    .quickReplies(Arrays.asList("Track CP-2026-000001", "Raise a complaint", "Anonymous mode info"))
                    .build();
        }

        // 6. Intent: Anonymous mode
        if (lower.contains("anonymous") || lower.contains("privacy") || lower.contains("secret") || lower.contains("identity")) {
            String reply = "Yes! You can toggle **'Anonymous Mode'** during submission. When enabled:\n\n"
                    + "• Municipal officers and public dashboards **never see your name, email, or phone number**.\n"
                    + "• You still receive notifications in your personal dashboard and can track progress.\n"
                    + "• Only verified system administrators can view account logs in case of legal requirements.";

            return BotMessageResponse.builder()
                    .intent("ANONYMOUS_INFO")
                    .reply(reply)
                    .quickReplies(Arrays.asList("File anonymous complaint", "How to file", "Privacy Policy"))
                    .build();
        }

        // Fallback response with helpful guidance
        String reply = "Hello! I am **CivicBot**, your civic assistance AI companion. I can help you with:\n\n"
                + "• Tracking any complaint (just type or paste its code, like `CP-2026-000001`)\n"
                + "• Explaining how to file and upload evidence\n"
                + "• Department information, working hours, and SLA resolution timelines\n\n"
                + "How can I assist you today?";

        return BotMessageResponse.builder()
                .intent("GENERAL_ASSISTANCE")
                .reply(reply)
                .quickReplies(Arrays.asList("How to file a complaint", "Working hours", "Track complaint", "Department info"))
                .build();
    }
}
