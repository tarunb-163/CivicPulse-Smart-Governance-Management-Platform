package com.civicpulse.service;

import com.civicpulse.dto.request.*;
import com.civicpulse.dto.response.*;
import com.civicpulse.entity.Complaint;
import com.civicpulse.entity.User;
import com.civicpulse.enums.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ComplaintService {
    ComplaintResponse createComplaint(CreateComplaintRequest request, List<MultipartFile> files, User citizen, String ipAddress);
    ComplaintResponse getComplaintById(Long id, User currentUser);
    ComplaintResponse getComplaintByPublicCode(String publicCode, User currentUser);
    Page<ComplaintResponse> getCitizenComplaints(Long citizenId, ComplaintStatus status, String query, Pageable pageable);
    Page<ComplaintResponse> getOfficerComplaints(Long departmentId, Long officerId, ComplaintStatus status, Long categoryId, boolean overdueOnly, String query, Pageable pageable);
    List<ComplaintResponse> getKanbanBoardComplaints(Long departmentId, Long officerId);

    ComplaintResponse changeStatus(Long complaintId, ComplaintStatus newStatus, String remark, User actor, String ipAddress);
    ComplaintResponse reassign(Long complaintId, ReassignRequest request, User actor, String ipAddress);
    ComplaintResponse uploadProof(Long complaintId, MultipartFile file, User officer, String ipAddress);
    ComplaintResponse reopen(Long complaintId, ReopenComplaintRequest request, User citizen, String ipAddress);

    ApiResponse supportComplaint(Long complaintId, User citizen);
    ApiResponse upvoteComplaint(Long complaintId, User citizen);
    CommentResponse addComment(Long complaintId, CommentRequest request, User author);
    FeedbackResponse submitFeedback(Long complaintId, FeedbackRequest request, User citizen, String ipAddress);

    ComplaintResponse mergeComplaint(Long childId, MergeComplaintRequest request, User officer, String ipAddress);
    ComplaintResponse unmergeComplaint(Long childId, User admin, String ipAddress);

    List<SimilarCandidateResponse> findSimilar(Double lat, Double lng, String text);
    byte[] getReceiptPdf(Long complaintId, User currentUser);

    Complaint getEntityById(Long id);
}
