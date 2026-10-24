package com.civicpulse.controller;

import com.civicpulse.dto.response.*;
import com.civicpulse.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Performance analytics, trends, leaderboards, and exports")
@PreAuthorize("hasAnyRole('OFFICER', 'DEPT_HEAD', 'ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    @Operation(summary = "Get high-level summary KPIs")
    public ResponseEntity<AnalyticsSummaryResponse> getSummary(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "90d") String range) {
        return ResponseEntity.ok(analyticsService.getSummary(departmentId, range));
    }

    @GetMapping("/trend")
    @Operation(summary = "Get complaint trend time series")
    public ResponseEntity<List<TrendDataPoint>> getTrend(
            @RequestParam(defaultValue = "30d") String range,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(analyticsService.getTrend(range, departmentId));
    }

    @GetMapping("/by-category")
    @Operation(summary = "Get complaint distribution by category")
    public ResponseEntity<List<CategoryDistributionResponse>> getByCategory(
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(analyticsService.getByCategory(departmentId));
    }

    @GetMapping("/by-department")
    @Operation(summary = "Get complaint distribution by department")
    public ResponseEntity<List<DepartmentPerformanceResponse>> getByDepartment() {
        return ResponseEntity.ok(analyticsService.getByDepartment());
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Get officer performance leaderboard")
    public ResponseEntity<List<LeaderboardItemResponse>> getLeaderboard() {
        return ResponseEntity.ok(analyticsService.getLeaderboard());
    }

    @GetMapping("/heatmap")
    @Operation(summary = "Get geographic density points for heatmap rendering")
    public ResponseEntity<List<HeatmapPointResponse>> getHeatmap(
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(analyticsService.getHeatmap(departmentId));
    }

    @GetMapping("/predictions")
    @Operation(summary = "Get AI/statistical hotspot predictions (1.5x surge over 4-week moving average)")
    public ResponseEntity<List<HotspotPredictionResponse>> getPredictions() {
        return ResponseEntity.ok(analyticsService.getPredictions());
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Export analytics report as CSV")
    public ResponseEntity<byte[]> exportCsv(@RequestParam(required = false) Long departmentId) {
        byte[] csvData = analyticsService.exportData("csv", departmentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"civicpulse-analytics.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    @GetMapping("/export/pdf")
    @Operation(summary = "Export analytics report as PDF")
    public ResponseEntity<byte[]> exportPdf(@RequestParam(required = false) Long departmentId) {
        byte[] pdfData = analyticsService.exportData("pdf", departmentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"civicpulse-analytics.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData);
    }
}
