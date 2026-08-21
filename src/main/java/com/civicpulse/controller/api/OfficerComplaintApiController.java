package com.civicpulse.controller.api;

import com.civicpulse.dto.ComplaintDetailsDTO;
import com.civicpulse.services.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/officer/complaints")
public class OfficerComplaintApiController {

    private final ComplaintService complaintService;

    @Autowired
    public OfficerComplaintApiController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    /**
     * GET /api/officer/complaints/{id}
     * Retrieves full complaint details for officer review.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ComplaintDetailsDTO> getComplaintById(@PathVariable("id") String id) {
        ComplaintDetailsDTO complaint = complaintService.getComplaintDetailsById(id);

        if (complaint == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(complaint);
    }
}