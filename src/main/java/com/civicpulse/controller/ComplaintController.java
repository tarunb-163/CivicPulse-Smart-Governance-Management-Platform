package com.civicpulse.controller;

import com.civicpulse.dto.request.*;
import com.civicpulse.dto.response.*;
import com.civicpulse.entity.Complaint;
import com.civicpulse.entity.User;
import com.civicpulse.enums.ComplaintStatus;
import com.civicpulse.repository.UserRepository;
import com.civicpulse.security.UserPrincipal;
import com.civicpulse.service.ClassifierService;
import com.civicpulse.service.ComplaintService;
import com.civicpulse.service.FileStorageService;
import com.civicpulse.service.ImageClassifierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaints", description = "Endpoints for creating, managing, tracking, and engaging with civic grievances")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final ClassifierService classifierService;
    private final ImageClassifierService imageClassifierService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    @Operation(summary = "Submit a new complaint with optional file uploads (multipart)")
    public ResponseEntity<ComplaintResponse> createComplaintMultipart(
            @ModelAttribute CreateComplaintRequest modelRequest,
            @RequestPart(value = "complaint", required = false) CreateComplaintRequest partRequest,
            @RequestParam(value = "files", required = false) List<MultipartFile> filesParam,
            @RequestPart(value = "files", required = false) List<MultipartFile> filesPart,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest
    ) {
        CreateComplaintRequest request = (partRequest != null && partRequest.getTitle() != null) ? partRequest : modelRequest;
        if (request == null || request.getTitle() == null) {
            String complaintParam = httpRequest.getParameter("complaint");
            if (complaintParam != null && !complaintParam.isBlank()) {
                try {
                    request = new com.fasterxml.jackson.databind.ObjectMapper().readValue(complaintParam, CreateComplaintRequest.class);
                } catch (Exception ignored) {}
            }
        }
        List<MultipartFile> files = (filesPart != null && !filesPart.isEmpty()) ? filesPart : filesParam;
        User citizen = userRepository.findById(principal.getId()).orElse(null);
        String ip = getClientIp(httpRequest);
        ComplaintResponse response = complaintService.createComplaint(request, files, citizen, ip);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    @Operation(summary = "Submit a new complaint as JSON")
    public ResponseEntity<ComplaintResponse> createComplaintJson(
            @Valid @RequestBody CreateComplaintRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest
    ) {
        User citizen = userRepository.findById(principal.getId()).orElse(null);
        String ip = getClientIp(httpRequest);
        ComplaintResponse response = complaintService.createComplaint(request, null, citizen, ip);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('CITIZEN', 'ADMIN')")
    @Operation(summary = "Get list of complaints filed by the authenticated citizen")
    public ResponseEntity<PagedResponse<ComplaintResponse>> getMyComplaints(
            @RequestParam(value = "status", required = false) ComplaintStatus status,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Pageable pageable = createPageable(page, size, sort);
        Page<ComplaintResponse> complaints = complaintService.getCitizenComplaints(principal.getId(), status, query, pageable);
        return ResponseEntity.ok(PagedResponse.from(complaints));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get complaint details by ID")
    public ResponseEntity<ComplaintResponse> getComplaintById(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = (principal != null) ? userRepository.findById(principal.getId()).orElse(null) : null;
        return ResponseEntity.ok(complaintService.getComplaintById(id, user));
    }

    @PostMapping("/classify")
    @Operation(summary = "Suggest category based on complaint text")
    public ResponseEntity<CategorySuggestion> classifyText(@RequestBody ClassifyTextRequest request) {
        return ResponseEntity.ok(classifierService.classify(request.getQueryText()));
    }

    @PostMapping(value = "/classify-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Suggest category from uploaded image (IMG-1)")
    public ResponseEntity<ImageClassificationResult> classifyImage(
            @RequestParam(value = "file", required = false) MultipartFile fileParam,
            @RequestPart(value = "file", required = false) MultipartFile filePart) {
        MultipartFile file = (filePart != null && !filePart.isEmpty()) ? filePart : fileParam;
        return ResponseEntity.ok(imageClassifierService.classifyImage(file));
    }

    @GetMapping("/similar")
    @Operation(summary = "Search for similar complaints within 300 meters")
    public ResponseEntity<List<SimilarCandidateResponse>> getSimilarComplaints(
            @RequestParam("lat") Double lat,
            @RequestParam("lng") Double lng,
            @RequestParam("text") String text
    ) {
        return ResponseEntity.ok(complaintService.findSimilar(lat, lng, text));
    }

    @PostMapping("/check-duplicates")
    @Operation(summary = "Check for duplicate/similar complaints nearby (DUP-2)")
    public ResponseEntity<List<SimilarCandidateResponse>> checkDuplicates(@RequestBody CheckDuplicatesRequest request) {
        return ResponseEntity.ok(complaintService.findSimilar(
                request.getEffectiveLat(),
                request.getEffectiveLng(),
                request.getText()
        ));
    }

    @PostMapping("/{id}/support")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Support (follow and upvote) a complaint idempotently (DUP-3)")
    public ResponseEntity<ApiResponse> supportComplaint(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = userRepository.findById(principal.getId()).orElse(null);
        return ResponseEntity.ok(complaintService.supportComplaint(id, user));
    }

    @PostMapping("/{id}/upvote")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upvote a public complaint")
    public ResponseEntity<ApiResponse> upvoteComplaint(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = userRepository.findById(principal.getId()).orElse(null);
        return ResponseEntity.ok(complaintService.upvoteComplaint(id, user));
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add a comment to the complaint thread")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable("id") Long id,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = userRepository.findById(principal.getId()).orElse(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(complaintService.addComment(id, request, user));
    }

    @PostMapping("/{id}/reopen")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Reopen a resolved complaint within 7 days")
    public ResponseEntity<ComplaintResponse> reopenComplaint(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReopenComplaintRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest
    ) {
        User user = userRepository.findById(principal.getId()).orElse(null);
        String ip = getClientIp(httpRequest);
        return ResponseEntity.ok(complaintService.reopen(id, request, user, ip));
    }

    @PostMapping("/{id}/feedback")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Submit one-time citizen feedback after resolution")
    public ResponseEntity<FeedbackResponse> submitFeedback(
            @PathVariable("id") Long id,
            @Valid @RequestBody FeedbackRequest request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest httpRequest
    ) {
        User user = userRepository.findById(principal.getId()).orElse(null);
        String ip = getClientIp(httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(complaintService.submitFeedback(id, request, user, ip));
    }

    @GetMapping("/{id}/receipt.pdf")
    @Operation(summary = "Download official acknowledgment PDF receipt with embedded QR code")
    public ResponseEntity<byte[]> downloadReceipt(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User user = (principal != null) ? userRepository.findById(principal.getId()).orElse(null) : null;
        byte[] pdfBytes = complaintService.getReceiptPdf(id, user);

        Complaint complaint = complaintService.getEntityById(id);
        String filename = "CivicPulse-Receipt-" + complaint.getPublicCode() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/attachments/{storedName}")
    @Operation(summary = "Stream uploaded evidence or proof attachment")
    public ResponseEntity<Resource> getAttachment(@PathVariable("storedName") String storedName) {
        Resource file = fileStorageService.loadAsResource(storedName);
        String contentType = "application/octet-stream";
        if (storedName.endsWith(".png")) contentType = "image/png";
        else if (storedName.endsWith(".jpg") || storedName.endsWith(".jpeg")) contentType = "image/jpeg";
        else if (storedName.endsWith(".webp")) contentType = "image/webp";
        else if (storedName.endsWith(".pdf")) contentType = "application/pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    private Pageable createPageable(int page, int size, String sortParam) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (sortParam != null && !sortParam.isBlank()) {
            String[] parts = sortParam.split(",");
            String field = parts[0];
            Sort.Direction dir = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1])) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(dir, field);
        }
        return PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), sort);
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
