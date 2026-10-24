package com.civicpulse.controller;

import com.civicpulse.dto.request.ClassifyTextRequest;
import com.civicpulse.dto.response.AnnouncementResponse;
import com.civicpulse.dto.response.CategoryResponse;
import com.civicpulse.dto.response.CategorySuggestion;
import com.civicpulse.dto.response.ComplaintResponse;
import com.civicpulse.dto.response.DepartmentResponse;
import com.civicpulse.dto.response.HeatmapPointResponse;
import com.civicpulse.dto.response.PublicStatsResponse;
import com.civicpulse.service.AdminService;
import com.civicpulse.service.AnalyticsService;
import com.civicpulse.service.ClassifierService;
import com.civicpulse.service.ComplaintService;
import com.civicpulse.service.QrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Public", description = "Publicly accessible endpoints (no auth required)")
public class PublicController {

    private final ComplaintService complaintService;
    private final AdminService adminService;
    private final AnalyticsService analyticsService;
    private final QrService qrService;
    private final ClassifierService classifierService;

    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    @PostMapping("/api/v1/public/classify")
    @Operation(summary = "Suggest category based on complaint text")
    public ResponseEntity<CategorySuggestion> classifyText(@RequestBody ClassifyTextRequest request) {
        return ResponseEntity.ok(classifierService.classify(request.getQueryText()));
    }

    @GetMapping({"/api/v1/public/complaints/track/{code}", "/api/v1/public/complaints/code/{code}"})
    @Operation(summary = "Track complaint publicly by tracking code (omits personal data)")
    public ResponseEntity<ComplaintResponse> trackComplaint(@PathVariable String code) {
        return ResponseEntity.ok(complaintService.getComplaintByPublicCode(code, null));
    }

    @GetMapping("/api/v1/public/stats")
    @Operation(summary = "Get high-level statistics for public transparency page")
    public ResponseEntity<PublicStatsResponse> getPublicStats() {
        return ResponseEntity.ok(analyticsService.getPublicStats());
    }

    @GetMapping("/api/v1/public/map-points")
    @Operation(summary = "Get sanitized map points for public transparency heatmap/pins")
    public ResponseEntity<List<HeatmapPointResponse>> getMapPoints() {
        return ResponseEntity.ok(analyticsService.getHeatmap(null));
    }

    @GetMapping("/api/v1/public/announcements")
    @Operation(summary = "Get active public broadcast announcements")
    public ResponseEntity<List<AnnouncementResponse>> getActiveAnnouncements() {
        return ResponseEntity.ok(adminService.getAnnouncements());
    }

    @GetMapping("/api/v1/public/departments")
    @Operation(summary = "Get list of active departments")
    public ResponseEntity<List<DepartmentResponse>> getDepartments() {
        return ResponseEntity.ok(adminService.getAllDepartments());
    }

    @GetMapping("/api/v1/public/categories")
    @Operation(summary = "Get list of active complaint categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(adminService.getAllCategories(null));
    }

    @GetMapping(value = {"/public/qr/{code}.png", "/api/v1/public/qr/{code}.png"}, produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Generate and return QR code PNG for a tracking code (QR-1)")
    public ResponseEntity<byte[]> getQrCode(@PathVariable String code) {
        String trackUrl = publicBaseUrl + "/track.html?code=" + code;
        byte[] qrBytes = qrService.generateQrPng(trackUrl, 250, 250);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"qr-" + code + ".png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(qrBytes);
    }
}
