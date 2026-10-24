package com.civicpulse.controller;

import com.civicpulse.dto.request.*;
import com.civicpulse.dto.response.*;
import com.civicpulse.entity.SystemSetting;
import com.civicpulse.enums.Role;
import com.civicpulse.scheduler.SlaScheduler;
import com.civicpulse.security.UserPrincipal;
import com.civicpulse.service.AdminService;
import com.civicpulse.service.ComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "System Administration endpoints (ADMIN only)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ComplaintService complaintService;
    private final SlaScheduler slaScheduler;

    // --- DEPARTMENTS ---
    @GetMapping("/departments")
    @Operation(summary = "Get all departments")
    public ResponseEntity<List<DepartmentResponse>> getDepartments() {
        return ResponseEntity.ok(adminService.getAllDepartments());
    }

    @PostMapping("/departments")
    @Operation(summary = "Create department")
    public ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody DepartmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createDepartment(request, principal.getUser(), ip));
    }

    @PutMapping("/departments/{id}")
    @Operation(summary = "Update department")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(adminService.updateDepartment(id, request, principal.getUser(), ip));
    }

    @DeleteMapping("/departments/{id}")
    @Operation(summary = "Deactivate/delete department")
    public ResponseEntity<ApiResponse> deleteDepartment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(adminService.deleteDepartment(id, principal.getUser(), ip));
    }

    // --- CATEGORIES ---
    @GetMapping("/categories")
    @Operation(summary = "Get all categories")
    public ResponseEntity<List<CategoryResponse>> getCategories(@RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(adminService.getAllCategories(departmentId));
    }

    @PostMapping("/categories")
    @Operation(summary = "Create category")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createCategory(request, principal.getUser(), ip));
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update category")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(adminService.updateCategory(id, request, principal.getUser(), ip));
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Deactivate category")
    public ResponseEntity<ApiResponse> deleteCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(adminService.deleteCategory(id, principal.getUser(), ip));
    }

    // --- KEYWORD RULES ---
    @GetMapping("/rules")
    @Operation(summary = "Get keyword rules")
    public ResponseEntity<List<KeywordRuleResponse>> getRules(@RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(adminService.getAllKeywordRules(categoryId));
    }

    @PostMapping("/rules")
    @Operation(summary = "Create keyword rule")
    public ResponseEntity<KeywordRuleResponse> createRule(
            @Valid @RequestBody KeywordRuleRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createKeywordRule(request, principal.getUser(), ip));
    }

    @PutMapping("/rules/{id}")
    @Operation(summary = "Update keyword rule")
    public ResponseEntity<KeywordRuleResponse> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody KeywordRuleRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(adminService.updateKeywordRule(id, request, principal.getUser(), ip));
    }

    @DeleteMapping("/rules/{id}")
    @Operation(summary = "Delete keyword rule")
    public ResponseEntity<ApiResponse> deleteRule(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(adminService.deleteKeywordRule(id, principal.getUser(), ip));
    }

    @PostMapping("/rules/test")
    @Operation(summary = "Test text classifier against current rules")
    public ResponseEntity<CategorySuggestion> testRules(@RequestBody Map<String, String> body) {
        String text = body.getOrDefault("text", "");
        return ResponseEntity.ok(adminService.testKeywordRules(text));
    }

    // --- USER MANAGEMENT ---
    @GetMapping("/users")
    @Operation(summary = "List users with role/dept filters")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(adminService.getUsers(search, role, departmentId, pageable));
    }

    @PostMapping("/users")
    @Operation(summary = "Create officer / department head / admin")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateOfficerRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createOfficer(request, principal.getUser(), ip));
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "Activate or deactivate a user")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        boolean enabled = body.getOrDefault("enabled", body.getOrDefault("active", true));
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(adminService.toggleUserStatus(id, enabled, principal.getUser(), ip));
    }

    @PutMapping("/users/{id}/role")
    @Operation(summary = "Change a user's role")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        Role role = Role.valueOf((String) body.get("role"));
        Long departmentId = body.get("departmentId") != null ? Long.valueOf(body.get("departmentId").toString()) : null;
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(adminService.changeUserRole(id, role, departmentId, principal.getUser(), ip));
    }

    // --- SLA POLICIES ---
    @GetMapping("/sla-policies")
    @Operation(summary = "Get SLA policies for all categories")
    public ResponseEntity<List<CategoryResponse>> getSlaPolicies() {
        return ResponseEntity.ok(adminService.getAllCategories(null));
    }

    @PutMapping("/sla-policies/{categoryId}")
    @Operation(summary = "Update SLA policy for category")
    public ResponseEntity<CategoryResponse> updateSlaPolicy(
            @PathVariable Long categoryId,
            @Valid @RequestBody SlaPolicyRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(adminService.updateSlaPolicy(categoryId, request, principal.getUser(), ip));
    }

    @PostMapping({"/sla/trigger", "/sla/trigger-scheduler"})
    @Operation(summary = "Trigger SLA check and escalation cycle on-demand")
    public ResponseEntity<Map<String, String>> triggerSlaScheduler() {
        slaScheduler.runSlaChecks();
        return ResponseEntity.ok(Map.of("message", "SLA escalation check completed successfully"));
    }

    // --- ANNOUNCEMENTS ---
    @GetMapping("/announcements")
    @Operation(summary = "List all announcements")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncements() {
        return ResponseEntity.ok(adminService.getAnnouncements());
    }

    @PostMapping("/announcements")
    @Operation(summary = "Create broadcast announcement")
    public ResponseEntity<AnnouncementResponse> createAnnouncement(
            @Valid @RequestBody AnnouncementRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createAnnouncement(request, principal.getUser(), ip));
    }

    @DeleteMapping("/announcements/{id}")
    @Operation(summary = "Delete announcement")
    public ResponseEntity<ApiResponse> deleteAnnouncement(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(adminService.deleteAnnouncement(id, principal.getUser(), ip));
    }

    // --- SYSTEM SETTINGS ---
    @GetMapping("/settings")
    @Operation(summary = "Get all system settings")
    public ResponseEntity<List<SystemSetting>> getSettings() {
        return ResponseEntity.ok(adminService.getAllSettings());
    }

    @PutMapping("/settings")
    @Operation(summary = "Update system setting")
    public ResponseEntity<SystemSetting> updateSetting(
            @Valid @RequestBody SystemSettingRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(adminService.updateSetting(request, principal.getUser(), ip));
    }

    // --- AUDIT LOGS ---
    @GetMapping("/audit-logs")
    @Operation(summary = "View audit logs with filters")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entity,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(adminService.getAuditLogs(action, entity, query, pageable));
    }

    // --- DEV OUTBOX ---
    @GetMapping({"/outbox", "/dev-outbox"})
    @Operation(summary = "View simulated dev emails")
    public ResponseEntity<Page<DevOutboxResponse>> getDevOutbox(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(adminService.getDevOutbox(pageable));
    }

    // --- UNMERGE (DUP-6) ---
    @PostMapping("/complaints/{id}/unmerge")
    @Operation(summary = "Admin unmerge of a previously merged complaint (DUP-6)")
    public ResponseEntity<ComplaintResponse> unmergeComplaint(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(complaintService.unmergeComplaint(id, principal.getUser(), ip));
    }


    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
