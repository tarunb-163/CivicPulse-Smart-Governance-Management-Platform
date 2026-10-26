package com.civicpulse;

import com.civicpulse.dto.response.AnalyticsSummaryResponse;
import com.civicpulse.service.AnalyticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
public class AdminDashboardSqlVerificationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AnalyticsService analyticsService;

    @Test
    @DisplayName("Verify 10 Admin Dashboard summary KPI metrics against direct SQL counts")
    void testAdminDashboardMetricsMatchDirectSql() {
        AnalyticsSummaryResponse summary = analyticsService.getSummary(null, "90d");
        Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);

        // 1. Total Complaints count
        Long sqlTotalComplaints = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM complaints WHERE created_at >= ?", Long.class, cutoff);
        assertThat(summary.getTotalComplaints())
                .as("Total complaints in dashboard must equal SQL count")
                .isEqualTo(sqlTotalComplaints);

        // 2. Pending Complaints count (SUBMITTED or ASSIGNED)
        Long sqlPendingComplaints = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM complaints WHERE status IN ('SUBMITTED', 'ASSIGNED') AND created_at >= ?", Long.class, cutoff);
        assertThat(summary.getPendingComplaints())
                .as("Pending complaints in dashboard must equal SQL count")
                .isEqualTo(sqlPendingComplaints);

        // 3. In Progress Complaints count
        Long sqlInProgressComplaints = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM complaints WHERE status = 'IN_PROGRESS' AND created_at >= ?", Long.class, cutoff);
        assertThat(summary.getInProgressComplaints())
                .as("In-progress complaints in dashboard must equal SQL count")
                .isEqualTo(sqlInProgressComplaints);

        // 4. Resolved Complaints count
        Long sqlResolvedComplaints = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM complaints WHERE status = 'RESOLVED' AND created_at >= ?", Long.class, cutoff);
        assertThat(summary.getResolvedComplaints())
                .as("Resolved complaints in dashboard must equal SQL count")
                .isEqualTo(sqlResolvedComplaints);

        // 5. Closed Complaints count
        Long sqlClosedComplaints = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM complaints WHERE status = 'CLOSED' AND created_at >= ?", Long.class, cutoff);
        assertThat(summary.getClosedComplaints())
                .as("Closed complaints in dashboard must equal SQL count")
                .isEqualTo(sqlClosedComplaints);

        // 6. Rejected Complaints count
        Long sqlRejectedComplaints = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM complaints WHERE status = 'REJECTED' AND created_at >= ?", Long.class, cutoff);
        assertThat(summary.getRejectedComplaints())
                .as("Rejected complaints in dashboard must equal SQL count")
                .isEqualTo(sqlRejectedComplaints);

        // 7. Escalated Complaints count
        Long sqlEscalatedComplaints = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM complaints WHERE status = 'ESCALATED' AND created_at >= ?", Long.class, cutoff);
        assertThat(summary.getEscalatedComplaints())
                .as("Escalated complaints in dashboard must equal SQL count")
                .isEqualTo(sqlEscalatedComplaints);

        // 8. Overdue / SLA Breached count
        Long sqlSlaBreached = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM complaints WHERE sla_due_at IS NOT NULL AND sla_due_at < ? AND status NOT IN ('RESOLVED', 'CLOSED', 'REJECTED') AND created_at >= ?",
                Long.class, Instant.now(), cutoff);
        assertThat(summary.getSlaBreachedCount())
                .as("SLA breached count in dashboard must equal SQL count")
                .isEqualTo(sqlSlaBreached);

        // 9. Total Citizens registered count
        Long sqlCitizensCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE role = 'CITIZEN'", Long.class);
        assertThat(summary.getTotalCitizens())
                .as("Total citizens in dashboard must equal SQL count")
                .isEqualTo(sqlCitizensCount);

        // 10. Total Officers count (OFFICER + DEPT_HEAD)
        Long sqlOfficersCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE role IN ('OFFICER', 'DEPT_HEAD')", Long.class);
        assertThat(summary.getTotalOfficers())
                .as("Total officers in dashboard must equal SQL count")
                .isEqualTo(sqlOfficersCount);
    }
}
