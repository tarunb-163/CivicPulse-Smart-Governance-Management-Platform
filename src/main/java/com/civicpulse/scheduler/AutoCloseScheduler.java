package com.civicpulse.scheduler;

import com.civicpulse.entity.Complaint;
import com.civicpulse.entity.StatusHistory;
import com.civicpulse.enums.ComplaintStatus;
import com.civicpulse.enums.NotificationType;
import com.civicpulse.repository.ComplaintRepository;
import com.civicpulse.repository.StatusHistoryRepository;
import com.civicpulse.service.AuditLogService;
import com.civicpulse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class AutoCloseScheduler {

    private final ComplaintRepository complaintRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    /**
     * Daily check at 2 AM to automatically close resolved complaints older than 7 days.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public int autoCloseResolvedComplaints() {
        Instant cutoff = Instant.now().minus(7, ChronoUnit.DAYS);
        List<Complaint> resolvedOlderThan7Days = complaintRepository.findByStatusAndResolvedAtBefore(ComplaintStatus.RESOLVED, cutoff);

        int closedCount = 0;
        for (Complaint complaint : resolvedOlderThan7Days) {
            complaint.setStatus(ComplaintStatus.CLOSED);
            complaint.setClosedAt(Instant.now());
            complaintRepository.save(complaint);

            StatusHistory history = StatusHistory.builder()
                    .complaint(complaint)
                    .fromStatus(ComplaintStatus.RESOLVED)
                    .toStatus(ComplaintStatus.CLOSED)
                    .remark("Auto-closed: 7-day citizen feedback and reopen window elapsed without objection.")
                    .changedByName("Auto-Close Scheduler")
                    .build();
            statusHistoryRepository.save(history);

            notificationService.createNotification(
                    complaint.getCitizen(),
                    NotificationType.STATUS_CHANGED,
                    "Complaint Closed: " + complaint.getPublicCode(),
                    "Your complaint was marked CLOSED following the 7-day satisfaction window.",
                    "/citizen/complaint.html?id=" + complaint.getId()
            );

            auditLogService.log(null, "system", "AUTO_CLOSE", "Complaint", complaint.getId().toString(), "Auto-closed after 7 days", null);
            closedCount++;
        }

        if (closedCount > 0) {
            log.info("AutoCloseScheduler ran: closed {} complaints.", closedCount);
        }
        return closedCount;
    }
}
