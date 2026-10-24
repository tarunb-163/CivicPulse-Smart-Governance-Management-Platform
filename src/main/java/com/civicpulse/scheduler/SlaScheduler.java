package com.civicpulse.scheduler;

import com.civicpulse.entity.Complaint;
import com.civicpulse.entity.StatusHistory;
import com.civicpulse.entity.User;
import com.civicpulse.enums.ComplaintStatus;
import com.civicpulse.enums.NotificationType;
import com.civicpulse.enums.Role;
import com.civicpulse.repository.ComplaintRepository;
import com.civicpulse.repository.StatusHistoryRepository;
import com.civicpulse.repository.UserRepository;
import com.civicpulse.service.AuditLogService;
import com.civicpulse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class SlaScheduler {

    private final ComplaintRepository complaintRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    private static final List<ComplaintStatus> ESCALATABLE_STATUSES = Arrays.asList(
            ComplaintStatus.SUBMITTED,
            ComplaintStatus.ASSIGNED,
            ComplaintStatus.IN_PROGRESS
    );

    @Transactional
    public int runSlaChecks() {
        return checkAndEscalateOverdueComplaints();
    }

    /**
     * Runs every 15 minutes to inspect open complaints past their SLA due date.
     */
    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public int checkAndEscalateOverdueComplaints() {
        Instant now = Instant.now();
        List<Complaint> overdueComplaints = complaintRepository.findByStatusInAndSlaDueAtBefore(ESCALATABLE_STATUSES, now);

        int escalatedCount = 0;
        for (Complaint complaint : overdueComplaints) {
            if (complaint.getEscalationLevel() == 0) {
                // Level 1: Escalate to Department Head
                complaint.setEscalationLevel(1);
                ComplaintStatus oldStatus = complaint.getStatus();
                complaint.setStatus(ComplaintStatus.ESCALATED);
                complaintRepository.save(complaint);

                StatusHistory history = StatusHistory.builder()
                        .complaint(complaint)
                        .fromStatus(oldStatus)
                        .toStatus(ComplaintStatus.ESCALATED)
                        .remark("SLA Overdue: Escalated to Department Head for priority action.")
                        .changedByName("SLA Auto-Escalation System")
                        .build();
                statusHistoryRepository.save(history);

                // Notify citizen
                notificationService.createNotification(
                        complaint.getCitizen(),
                        NotificationType.COMPLAINT_ESCALATED,
                        "SLA Escalation: " + complaint.getPublicCode(),
                        "Your complaint has missed the standard SLA resolution time and was automatically escalated to the Department Head.",
                        "/citizen/complaint.html?id=" + complaint.getId()
                );

                // Notify Department Head
                if (complaint.getDepartment() != null) {
                    List<User> heads = userRepository.findByDepartmentIdAndRoleAndEnabledTrue(complaint.getDepartment().getId(), Role.DEPT_HEAD);
                    for (User head : heads) {
                        notificationService.createNotification(
                                head,
                                NotificationType.COMPLAINT_ESCALATED,
                                "OVERDUE SLA: " + complaint.getPublicCode(),
                                "Complaint '" + complaint.getTitle() + "' in your department is past SLA and has been escalated to you.",
                                "/officer/workspace.html?id=" + complaint.getId()
                        );
                    }
                }

                auditLogService.log(null, "system", "SLA_ESCALATE_L1", "Complaint", complaint.getId().toString(), "Escalated to Dept Head (overdue)", null);
                escalatedCount++;
            } else if (complaint.getEscalationLevel() == 1) {
                // Level 2: Escalate to Admin
                complaint.setEscalationLevel(2);
                complaintRepository.save(complaint);

                StatusHistory history = StatusHistory.builder()
                        .complaint(complaint)
                        .fromStatus(ComplaintStatus.ESCALATED)
                        .toStatus(ComplaintStatus.ESCALATED)
                        .remark("Critical SLA Delay: Escalated to System Administrator.")
                        .changedByName("SLA Auto-Escalation System")
                        .build();
                statusHistoryRepository.save(history);

                // Notify Admins
                List<User> admins = userRepository.findByRoleAndEnabledTrue(Role.ADMIN);
                for (User admin : admins) {
                    notificationService.createNotification(
                            admin,
                            NotificationType.COMPLAINT_ESCALATED,
                            "CRITICAL SLA BREACH: " + complaint.getPublicCode(),
                            "Complaint '" + complaint.getTitle() + "' is severely overdue and escalated to Admin.",
                            "/officer/workspace.html?id=" + complaint.getId()
                    );
                }

                auditLogService.log(null, "system", "SLA_ESCALATE_L2", "Complaint", complaint.getId().toString(), "Critical breach escalated to Admin", null);
                escalatedCount++;
            }
        }

        if (escalatedCount > 0) {
            log.info("SlaScheduler ran: escalated {} overdue complaints.", escalatedCount);
        }
        return escalatedCount;
    }
}
