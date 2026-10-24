package com.civicpulse.service.impl;

import com.civicpulse.dto.response.*;
import com.civicpulse.entity.Category;
import com.civicpulse.entity.Complaint;
import com.civicpulse.entity.Department;
import com.civicpulse.enums.ComplaintStatus;
import com.civicpulse.enums.Role;
import com.civicpulse.enums.Urgency;
import com.civicpulse.exception.BadRequestException;
import com.civicpulse.repository.*;
import com.civicpulse.service.AnalyticsService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ComplaintRepository complaintRepository;
    private final DepartmentRepository departmentRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final FeedbackRepository feedbackRepository;

    @Override
    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getSummary(Long departmentId, String range) {
        List<Complaint> complaints = getFilteredComplaints(departmentId, range);

        long total = complaints.size();
        long pending = complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.SUBMITTED || c.getStatus() == ComplaintStatus.ASSIGNED).count();
        long inProgress = complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.IN_PROGRESS).count();
        long resolved = complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.RESOLVED).count();
        long closed = complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.CLOSED).count();
        long rejected = complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.REJECTED).count();
        long escalated = complaints.stream().filter(c -> c.getStatus() == ComplaintStatus.ESCALATED).count();

        Instant now = Instant.now();
        long overdue = complaints.stream().filter(c -> c.getSlaDueAt() != null && c.getSlaDueAt().isBefore(now)
                && c.getStatus() != ComplaintStatus.RESOLVED && c.getStatus() != ComplaintStatus.CLOSED && c.getStatus() != ComplaintStatus.REJECTED).count();

        // Avg resolution hours
        List<Complaint> resolvedList = complaints.stream()
                .filter(c -> (c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED) && c.getResolvedAt() != null)
                .collect(Collectors.toList());

        double avgHours = 0.0;
        if (!resolvedList.isEmpty()) {
            double totalHours = resolvedList.stream()
                    .mapToDouble(c -> Duration.between(c.getCreatedAt(), c.getResolvedAt()).toMinutes() / 60.0)
                    .sum();
            avgHours = Math.round((totalHours / resolvedList.size()) * 10.0) / 10.0;
        }

        double resRate = total > 0 ? Math.round(((double) (resolved + closed) / total) * 1000.0) / 10.0 : 0.0;

        long citizensCount = userRepository.countByRole(Role.CITIZEN);
        long officersCount = userRepository.countByRole(Role.OFFICER) + userRepository.countByRole(Role.DEPT_HEAD);

        Double avgRating = feedbackRepository.findOverallAverageRating();
        double ratingVal = avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 4.5;

        return AnalyticsSummaryResponse.builder()
                .totalComplaints(total)
                .pendingComplaints(pending)
                .inProgressComplaints(inProgress)
                .resolvedComplaints(resolved)
                .closedComplaints(closed)
                .rejectedComplaints(rejected)
                .escalatedComplaints(escalated)
                .slaBreachedCount(overdue)
                .avgResolutionHours(avgHours)
                .resolutionRatePercentage(resRate)
                .totalCitizens(citizensCount)
                .totalOfficers(officersCount)
                .overallCitizenRating(ratingVal)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrendDataPoint> getTrend(String range, Long departmentId) {
        int days = parseRangeDays(range);
        LocalDate endDate = LocalDate.now(ZoneId.of("UTC"));
        LocalDate startDate = endDate.minusDays(days);

        Instant startInstant = startDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
        List<Complaint> complaints = complaintRepository.findAll().stream()
                .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(startInstant))
                .filter(c -> departmentId == null || (c.getDepartment() != null && c.getDepartment().getId().equals(departmentId)))
                .collect(Collectors.toList());

        Map<String, Long> submittedMap = new HashMap<>();
        Map<String, Long> resolvedMap = new HashMap<>();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Complaint c : complaints) {
            String subDate = LocalDate.ofInstant(c.getCreatedAt(), ZoneId.of("UTC")).format(fmt);
            submittedMap.merge(subDate, 1L, Long::sum);

            if (c.getResolvedAt() != null) {
                String resDate = LocalDate.ofInstant(c.getResolvedAt(), ZoneId.of("UTC")).format(fmt);
                resolvedMap.merge(resDate, 1L, Long::sum);
            }
        }

        List<TrendDataPoint> points = new ArrayList<>();
        for (int i = 0; i <= days; i++) {
            LocalDate d = startDate.plusDays(i);
            String dateStr = d.format(fmt);
            points.add(TrendDataPoint.builder()
                    .date(dateStr)
                    .submittedCount(submittedMap.getOrDefault(dateStr, 0L))
                    .resolvedCount(resolvedMap.getOrDefault(dateStr, 0L))
                    .build());
        }

        return points;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDistributionResponse> getByCategory(Long departmentId) {
        List<Complaint> complaints = complaintRepository.findAll().stream()
                .filter(c -> departmentId == null || (c.getDepartment() != null && c.getDepartment().getId().equals(departmentId)))
                .collect(Collectors.toList());

        long total = complaints.size();
        if (total == 0) return Collections.emptyList();

        Map<Category, Long> categoryCount = complaints.stream()
                .filter(c -> c.getCategory() != null)
                .collect(Collectors.groupingBy(Complaint::getCategory, Collectors.counting()));

        return categoryCount.entrySet().stream()
                .map(e -> {
                    Category cat = e.getKey();
                    long count = e.getValue();
                    double pct = Math.round(((double) count / total) * 1000.0) / 10.0;
                    return CategoryDistributionResponse.builder()
                            .categoryId(cat.getId())
                            .categoryName(cat.getName())
                            .departmentName(cat.getDepartment() != null ? cat.getDepartment().getName() : "General")
                            .count(count)
                            .percentage(pct)
                            .build();
                })
                .sorted(Comparator.comparingLong(CategoryDistributionResponse::getCount).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentPerformanceResponse> getByDepartment() {
        List<Department> departments = departmentRepository.findAll();
        List<Complaint> complaints = complaintRepository.findAll();

        Map<Long, List<Complaint>> deptComplaints = complaints.stream()
                .filter(c -> c.getDepartment() != null)
                .collect(Collectors.groupingBy(c -> c.getDepartment().getId()));

        return departments.stream().map(dept -> {
            List<Complaint> list = deptComplaints.getOrDefault(dept.getId(), Collections.emptyList());
            long total = list.size();
            long resolved = list.stream().filter(c -> c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED).count();
            long pending = total - resolved;
            double resRate = total > 0 ? Math.round(((double) resolved / total) * 1000.0) / 10.0 : 0.0;

            List<Complaint> resolvedList = list.stream()
                    .filter(c -> (c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED) && c.getResolvedAt() != null)
                    .collect(Collectors.toList());
            double avgHours = 0.0;
            if (!resolvedList.isEmpty()) {
                double sum = resolvedList.stream().mapToDouble(c -> Duration.between(c.getCreatedAt(), c.getResolvedAt()).toMinutes() / 60.0).sum();
                avgHours = Math.round((sum / resolvedList.size()) * 10.0) / 10.0;
            }

            return DepartmentPerformanceResponse.builder()
                    .departmentId(dept.getId())
                    .departmentName(dept.getName())
                    .departmentCode(dept.getCode())
                    .total(total)
                    .resolved(resolved)
                    .pending(pending)
                    .resolutionRate(resRate)
                    .avgResolutionHours(avgHours)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardItemResponse> getLeaderboard() {
        List<DepartmentPerformanceResponse> deptPerf = getByDepartment();
        List<LeaderboardItemResponse> items = new ArrayList<>();

        for (DepartmentPerformanceResponse dp : deptPerf) {
            Double avgRating = feedbackRepository.findAverageRatingByDepartment(dp.getDepartmentId());
            double rating = avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 4.2;

            // Score formula: (resolutionRate * 0.45) + (rating * 20 * 0.35) + max(0, 100 - avgHours) * 0.20
            double timeComponent = Math.max(0.0, 100.0 - dp.getAvgResolutionHours());
            double compositeScore = (dp.getResolutionRate() * 0.45) + ((rating / 5.0 * 100.0) * 0.35) + (timeComponent * 0.20);
            compositeScore = Math.round(compositeScore * 10.0) / 10.0;

            items.add(LeaderboardItemResponse.builder()
                    .departmentId(dp.getDepartmentId())
                    .departmentName(dp.getDepartmentName())
                    .departmentCode(dp.getDepartmentCode())
                    .totalHandled(dp.getTotal())
                    .resolutionRate(dp.getResolutionRate())
                    .avgResolutionHours(dp.getAvgResolutionHours())
                    .avgRating(rating)
                    .score(compositeScore)
                    .build());
        }

        items.sort(Comparator.comparingDouble(LeaderboardItemResponse::getScore).reversed());
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setRank(i + 1);
        }
        return items;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HeatmapPointResponse> getHeatmap(Long departmentId) {
        List<Complaint> complaints = complaintRepository.findAll().stream()
                .filter(c -> c.getLatitude() != null && c.getLongitude() != null)
                .filter(c -> departmentId == null || (c.getDepartment() != null && c.getDepartment().getId().equals(departmentId)))
                .collect(Collectors.toList());

        return complaints.stream().map(c -> {
            double intensity = switch (c.getUrgency() != null ? c.getUrgency() : Urgency.MEDIUM) {
                case LOW -> 0.3;
                case MEDIUM -> 0.6;
                case HIGH -> 0.8;
                case CRITICAL -> 1.0;
            };
            return HeatmapPointResponse.builder()
                    .lat(c.getLatitude())
                    .lng(c.getLongitude())
                    .intensity(intensity)
                    .status(c.getStatus().name())
                    .urgency(c.getUrgency().name())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotspotPredictionResponse> getPredictions() {
        // Hotspot prediction rule (ANA-6):
        // 4-week moving average (complaints in days 8..35) vs last week (days 0..7)
        // Flag spike if lastWeekCount >= 1.5 * fourWeekAverageWeekly
        Instant now = Instant.now();
        Instant lastWeekStart = now.minus(7, ChronoUnit.DAYS);
        Instant fourWeeksPriorStart = now.minus(35, ChronoUnit.DAYS);

        List<Complaint> allRecent = complaintRepository.findAll().stream()
                .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(fourWeeksPriorStart))
                .collect(Collectors.toList());

        Map<String, List<Complaint>> byCategory = allRecent.stream()
                .filter(c -> c.getCategory() != null)
                .collect(Collectors.groupingBy(c -> c.getCategory().getName()));

        List<HotspotPredictionResponse> predictions = new ArrayList<>();

        for (Map.Entry<String, List<Complaint>> entry : byCategory.entrySet()) {
            String categoryName = entry.getKey();
            List<Complaint> catComplaints = entry.getValue();
            Department dept = catComplaints.get(0).getDepartment();

            long lastWeekCount = catComplaints.stream()
                    .filter(c -> c.getCreatedAt().isAfter(lastWeekStart))
                    .count();

            long previousFourWeeksCount = catComplaints.stream()
                    .filter(c -> c.getCreatedAt().isBefore(lastWeekStart))
                    .count();

            double fourWeekAvgWeekly = Math.round((previousFourWeeksCount / 4.0) * 10.0) / 10.0;
            if (fourWeekAvgWeekly == 0) fourWeekAvgWeekly = 1.0; // avoid divide by zero

            double ratio = Math.round((lastWeekCount / fourWeekAvgWeekly) * 100.0) / 100.0;
            boolean spikeAlert = lastWeekCount >= 3 && ratio >= 1.5;

            String rec = spikeAlert
                    ? "Immediate pre-deployment of maintenance squads suggested in Ward 4 & Ward 7."
                    : "Normal seasonal trend. Monitor weekly SLA compliance.";

            predictions.add(HotspotPredictionResponse.builder()
                    .categoryName(categoryName)
                    .departmentName(dept != null ? dept.getName() : "General")
                    .area("Davanagere Central & Suburbs")
                    .fourWeekAverageWeekly(fourWeekAvgWeekly)
                    .lastWeekCount(lastWeekCount)
                    .ratio(ratio)
                    .spikeAlert(spikeAlert)
                    .recommendation(rec)
                    .build());
        }

        // Sort so spike alerts appear first
        predictions.sort((a, b) -> Boolean.compare(b.isSpikeAlert(), a.isSpikeAlert()));
        return predictions;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportData(String format, Long departmentId) {
        List<Complaint> complaints = complaintRepository.findAll().stream()
                .filter(c -> departmentId == null || (c.getDepartment() != null && c.getDepartment().getId().equals(departmentId)))
                .sorted(Comparator.comparing(Complaint::getCreatedAt).reversed())
                .collect(Collectors.toList());

        if ("csv".equalsIgnoreCase(format)) {
            return exportCsv(complaints);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return exportPdf(complaints);
        } else {
            throw new BadRequestException("Unsupported format: " + format + ". Allowed: csv, pdf");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PublicStatsResponse getPublicStats() {
        long total = complaintRepository.count();
        long resolved = complaintRepository.countResolvedComplaints();
        double resRate = total > 0 ? Math.round(((double) resolved / total) * 1000.0) / 10.0 : 0.0;

        List<Complaint> resolvedList = complaintRepository.findAll().stream()
                .filter(c -> (c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED) && c.getResolvedAt() != null)
                .collect(Collectors.toList());

        double avgHours = 0.0;
        if (!resolvedList.isEmpty()) {
            double sum = resolvedList.stream().mapToDouble(c -> Duration.between(c.getCreatedAt(), c.getResolvedAt()).toMinutes() / 60.0).sum();
            avgHours = Math.round((sum / resolvedList.size()) * 10.0) / 10.0;
        }

        Double rating = feedbackRepository.findOverallAverageRating();
        double citizenRating = rating != null ? Math.round(rating * 10.0) / 10.0 : 4.5;

        List<DepartmentPerformanceResponse> deptStats = getByDepartment();

        return PublicStatsResponse.builder()
                .totalGrievances(total)
                .resolvedGrievances(resolved)
                .resolutionRatePercentage(resRate)
                .avgResolutionHours(avgHours)
                .citizenSatisfactionRating(citizenRating)
                .departmentStats(deptStats)
                .build();
    }

    private byte[] exportCsv(List<Complaint> complaints) {
        try (StringWriter sw = new StringWriter();
             CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder()
                     .setHeader("Code", "Title", "Category", "Department", "Status", "Urgency", "Priority", "Address", "Created Date", "Resolved Date")
                     .build())) {

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Complaint c : complaints) {
                printer.printRecord(
                        c.getPublicCode(),
                        c.getTitle(),
                        c.getCategory() != null ? c.getCategory().getName() : "",
                        c.getDepartment() != null ? c.getDepartment().getName() : "",
                        c.getStatus().name(),
                        c.getUrgency().name(),
                        c.getPriorityScore(),
                        c.getAddress() != null ? c.getAddress() : "",
                        c.getCreatedAt() != null ? fmt.format(c.getCreatedAt().atZone(ZoneId.of("Asia/Kolkata"))) : "",
                        c.getResolvedAt() != null ? fmt.format(c.getResolvedAt().atZone(ZoneId.of("Asia/Kolkata"))) : ""
                );
            }
            printer.flush();
            return sw.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to generate CSV export", e);
            throw new RuntimeException("Failed to generate CSV", e);
        }
    }

    private byte[] exportPdf(List<Complaint> complaints) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(37, 99, 235));
            Paragraph header = new Paragraph("CivicPulse Grievance Analytics & Resolution Report", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            header.setSpacingAfter(15);
            document.add(header);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{15f, 25f, 15f, 15f, 12f, 9f, 9f});

            Font thFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            String[] headers = {"Code", "Title", "Department", "Category", "Status", "Urgency", "Score"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, thFont));
                cell.setBackgroundColor(new Color(30, 41, 59));
                cell.setPadding(6);
                table.addCell(cell);
            }

            Font tdFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            for (Complaint c : complaints.stream().limit(200).collect(Collectors.toList())) {
                table.addCell(new Phrase(c.getPublicCode(), tdFont));
                table.addCell(new Phrase(c.getTitle(), tdFont));
                table.addCell(new Phrase(c.getDepartment() != null ? c.getDepartment().getName() : "", tdFont));
                table.addCell(new Phrase(c.getCategory() != null ? c.getCategory().getName() : "", tdFont));
                table.addCell(new Phrase(c.getStatus().name(), tdFont));
                table.addCell(new Phrase(c.getUrgency().name(), tdFont));
                table.addCell(new Phrase(String.valueOf(c.getPriorityScore()), tdFont));
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF report", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private List<Complaint> getFilteredComplaints(Long departmentId, String range) {
        int days = parseRangeDays(range);
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);

        return complaintRepository.findAll().stream()
                .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(cutoff))
                .filter(c -> departmentId == null || (c.getDepartment() != null && c.getDepartment().getId().equals(departmentId)))
                .collect(Collectors.toList());
    }

    private int parseRangeDays(String range) {
        if (range == null) return 90;
        return switch (range.toLowerCase().trim()) {
            case "7d", "7" -> 7;
            case "30d", "30" -> 30;
            case "90d", "90" -> 90;
            case "1y", "365d" -> 365;
            default -> 90;
        };
    }
}
