package com.civicpulse.controller;

import com.civicpulse.dto.request.CommentRequest;
import com.civicpulse.dto.request.MergeComplaintRequest;
import com.civicpulse.dto.request.ReassignRequest;
import com.civicpulse.dto.request.RejectComplaintRequest;
import com.civicpulse.dto.request.StatusUpdateRequest;
import com.civicpulse.dto.response.CommentResponse;
import com.civicpulse.dto.response.ComplaintResponse;
import com.civicpulse.entity.User;
import com.civicpulse.enums.ComplaintStatus;
import com.civicpulse.enums.Role;
import com.civicpulse.repository.ComplaintRepository;
import com.civicpulse.security.UserPrincipal;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/officer")
@RequiredArgsConstructor
@Tag(name = "Officer", description = "Endpoints for officers and department heads")
@PreAuthorize("hasAnyRole('OFFICER', 'DEPT_HEAD', 'ADMIN')")
public class OfficerController {

    private final ComplaintService complaintService;
    private final ComplaintRepository complaintRepository;
    private final com.civicpulse.repository.UserRepository userRepository;

    @GetMapping({"/queue", "/complaints"})
    @Operation(summary = "Get officer complaint queue with filters and pagination")
    public ResponseEntity<Page<ComplaintResponse>> getQueue(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean overdueOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "priorityScore,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ?
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        User user = principal.getUser();
        Long deptId = user.getDepartment() != null ? user.getDepartment().getId() : null;
        Long officerId = user.getRole() == Role.OFFICER ? user.getId() : null;

        Page<ComplaintResponse> result = complaintService.getOfficerComplaints(
                deptId, officerId, status, categoryId, overdueOnly, search, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping({"/board", "/kanban"})
    @Operation(summary = "Get Kanban board grouped complaints for officer's department")
    public ResponseEntity<Map<String, Object>> getBoard(
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getId()).orElse(principal.getUser());
        Long deptId = user.getDepartment() != null ? user.getDepartment().getId() : null;
        Long officerId = user.getRole() == Role.OFFICER ? user.getId() : null;
        List<ComplaintResponse> list = complaintService.getKanbanBoardComplaints(deptId, officerId);

        Map<String, Object> map = new HashMap<>();
        map.put("all", list);
        map.put("assigned", list.stream().filter(c -> c.getStatus() == ComplaintStatus.ASSIGNED || c.getStatus() == ComplaintStatus.SUBMITTED).toList());
        map.put("inProgress", list.stream().filter(c -> c.getStatus() == ComplaintStatus.IN_PROGRESS || c.getStatus() == ComplaintStatus.ESCALATED).toList());
        map.put("resolved", list.stream().filter(c -> c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED).toList());
        return ResponseEntity.ok(map);
    }

    @GetMapping("/complaints/{id}")
    @Operation(summary = "Get detailed complaint for officer work drawer")
    public ResponseEntity<ComplaintResponse> getComplaintDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(complaintService.getComplaintById(id, principal.getUser()));
    }

    @PostMapping(value = "/complaints/{id}/status", consumes = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update complaint status with remarks (JSON)")
    public ResponseEntity<ComplaintResponse> updateStatusJson(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(complaintService.changeStatus(id, request.getStatus(), request.getRemark(), principal.getUser(), ip));
    }

    @PostMapping(value = "/complaints/{id}/status", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update complaint status with remarks and optional proof attachment (multipart)")
    public ResponseEntity<ComplaintResponse> updateStatusMultipart(
            @PathVariable Long id,
            @RequestParam("status") ComplaintStatus status,
            @RequestParam(value = "remark", defaultValue = "") String remark,
            @RequestParam(value = "proof", required = false) org.springframework.web.multipart.MultipartFile proofParam,
            @RequestPart(value = "proof", required = false) org.springframework.web.multipart.MultipartFile proofPart,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        org.springframework.web.multipart.MultipartFile proof = (proofPart != null && !proofPart.isEmpty()) ? proofPart : proofParam;
        if (proof != null && !proof.isEmpty()) {
            complaintService.uploadProof(id, proof, principal.getUser(), ip);
        }
        return ResponseEntity.ok(complaintService.changeStatus(id, status, remark, principal.getUser(), ip));
    }

    @PostMapping("/complaints/{id}/notes")
    @Operation(summary = "Add an internal note to the complaint")
    public ResponseEntity<CommentResponse> addInternalNote(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        request.setInternal(true);
        return ResponseEntity.ok(complaintService.addComment(id, request, principal.getUser()));
    }

    @PostMapping("/complaints/{id}/reassign")
    @Operation(summary = "Reassign complaint to another officer with mandatory reason")
    public ResponseEntity<ComplaintResponse> reassign(
            @PathVariable Long id,
            @Valid @RequestBody ReassignRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(complaintService.reassign(id, request, principal.getUser(), ip));
    }

    @PostMapping("/complaints/{id}/reject")
    @Operation(summary = "Reject complaint with mandatory reason")
    public ResponseEntity<ComplaintResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectComplaintRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(complaintService.changeStatus(id, ComplaintStatus.REJECTED, request.getReason(), principal.getUser(), ip));
    }

    @PostMapping("/complaints/{id}/merge")
    @Operation(summary = "Merge duplicate complaint into parent complaint (DUP-5)")
    public ResponseEntity<ComplaintResponse> mergeComplaint(
            @PathVariable Long id,
            @Valid @RequestBody MergeComplaintRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(complaintService.mergeComplaint(id, request, principal.getUser(), ip));
    }

    @GetMapping("/stats")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @Operation(summary = "Get officer performance statistics")
    public ResponseEntity<Map<String, Object>> getOfficerStats(
            @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getId()).orElse(principal.getUser());
        long activeCount = complaintRepository.countActiveComplaintsByOfficer(user.getId(), Set.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS, ComplaintStatus.ESCALATED));
        long resolvedTotal = complaintRepository.countResolvedComplaints();

        Map<String, Object> stats = new HashMap<>();
        stats.put("activeAssigned", activeCount);
        stats.put("totalResolved", resolvedTotal);
        String deptName = "General";
        try {
            if (user.getDepartment() != null) {
                deptName = user.getDepartment().getName();
            }
        } catch (Exception ignored) {}
        stats.put("department", deptName);
        return ResponseEntity.ok(stats);
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
