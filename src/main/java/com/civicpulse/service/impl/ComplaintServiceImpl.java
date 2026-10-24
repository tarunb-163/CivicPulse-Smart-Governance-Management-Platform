package com.civicpulse.service.impl;

import com.civicpulse.dto.request.*;
import com.civicpulse.dto.response.*;
import com.civicpulse.entity.*;
import com.civicpulse.enums.*;
import com.civicpulse.exception.*;
import com.civicpulse.repository.*;
import com.civicpulse.service.*;
import com.civicpulse.util.CodeGenerator;
import com.civicpulse.util.ContentNormalizer;
import com.civicpulse.util.GeoUtils;
import com.civicpulse.util.ImageHashUtil;
import com.civicpulse.util.TextSimilarity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;
    private final AttachmentRepository attachmentRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final CommentRepository commentRepository;
    private final UpvoteRepository upvoteRepository;
    private final ComplaintFollowerRepository complaintFollowerRepository;
    private final FeedbackRepository feedbackRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final UserRepository userRepository;

    private final ClassifierService classifierService;
    private final PriorityService priorityService;
    private final AssignmentService assignmentService;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final CodeGenerator codeGenerator;
    private final PdfReceiptService pdfReceiptService;

    @Value("${app.duplicate.radius-meters:300.0}")
    private double defaultRadiusMeters;

    @Value("${app.duplicate.similarity-threshold:0.60}")
    private double defaultSimilarityThreshold;

    @Value("${app.rate-limit.complaints-per-hour:5}")
    private int maxComplaintsPerHour;

    @Value("${app.rate-limit.complaints-per-day:15}")
    private int maxComplaintsPerDay;

    private static final List<ComplaintStatus> OPEN_STATUSES = Arrays.asList(
            ComplaintStatus.SUBMITTED,
            ComplaintStatus.ASSIGNED,
            ComplaintStatus.IN_PROGRESS,
            ComplaintStatus.ESCALATED,
            ComplaintStatus.REOPENED
    );

    @Override
    @Transactional
    public ComplaintResponse createComplaint(CreateComplaintRequest request, List<MultipartFile> files, User citizen, String ipAddress) {
        if (citizen == null) {
            throw new BadRequestException("Authenticated citizen is required to submit a complaint");
        }

        // 1. Rate limiting checks (DUP-6)
        long countLastHour = complaintRepository.countByCitizenIdAndCreatedAtAfter(citizen.getId(), Instant.now().minus(1, ChronoUnit.HOURS));
        if (countLastHour >= maxComplaintsPerHour) {
            throw new RateLimitExceededException("Hourly complaint submission limit exceeded (" + maxComplaintsPerHour + "/hour). Please wait.", 3600);
        }

        long countLastDay = complaintRepository.countByCitizenIdAndCreatedAtAfter(citizen.getId(), Instant.now().minus(24, ChronoUnit.HOURS));
        if (countLastDay >= maxComplaintsPerDay) {
            throw new RateLimitExceededException("Daily complaint submission limit exceeded (" + maxComplaintsPerDay + "/day).", 86400);
        }

        // 2. Exact-duplicate guard (DUP-1)
        String contentHash = ContentNormalizer.computeHash(request.getTitle(), request.getDescription());
        List<Complaint> exactDupes = complaintRepository.findByCitizenIdAndContentHashAndCreatedAtAfter(
                citizen.getId(), contentHash, Instant.now().minus(24, ChronoUnit.HOURS));
        if (!exactDupes.isEmpty()) {
            Complaint existing = exactDupes.get(0);
            throw DuplicateComplaintException.exact(
                    "You have already submitted an identical complaint within the last 24 hours (" + existing.getPublicCode() + ")",
                    existing.getPublicCode()
            );
        }

        // 3. Similar-complaint nearby guard (DUP-2)
        if (!request.isConfirmNotDuplicate() && request.getLatitude() != null && request.getLongitude() != null) {
            List<SimilarCandidateResponse> similar = findSimilar(request.getLatitude(), request.getLongitude(), request.getTitle() + " " + request.getDescription());
            if (!similar.isEmpty()) {
                throw DuplicateComplaintException.suspected(
                        "Similar open complaint(s) found nearby. Please check if this issue has already been reported.",
                        similar
                );
            }
        }

        // 4. Resolve Category and Department
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BadRequestException("Invalid category ID: " + request.getCategoryId()));
        } else {
            CategorySuggestion suggestion = classifierService.classify(request.getTitle() + " " + request.getDescription());
            if (suggestion.getCategoryId() != null) {
                category = categoryRepository.findById(suggestion.getCategoryId()).orElse(null);
            }
        }

        if (category == null) {
            // Fallback to first active category or general
            category = categoryRepository.findByNameIgnoreCase("Miscellaneous Civic Grievance")
                    .orElseGet(() -> categoryRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> new BadRequestException("No category configured in the system")));
        }

        Department department = category.getDepartment();
        if (department == null) {
            department = departmentRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new BadRequestException("No department configured in the system"));
        }

        // 5. Generate unique public code
        String publicCode = codeGenerator.generateComplaintCode();

        // 6. SLA due calculation
        int slaHours = category.getDefaultSlaHours() > 0 ? category.getDefaultSlaHours() : department.getDefaultSlaHours();
        Instant slaDueAt = Instant.now().plus(slaHours, ChronoUnit.HOURS);

        // 7. Auto-assignment (RTE-4)
        User assignedOfficer = assignmentService.assignLeastLoadedOfficer(department);
        ComplaintStatus initialStatus = (assignedOfficer != null) ? ComplaintStatus.ASSIGNED : ComplaintStatus.SUBMITTED;

        // 8. Priority score calculation (RTE-3)
        double initialPriority = priorityService.computePriority(request.getUrgency(), category.getSeverityWeight(), 0, 0.0);

        // 9. Build Complaint
        Complaint complaint = Complaint.builder()
                .publicCode(publicCode)
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .category(category)
                .department(department)
                .citizen(citizen)
                .assignedOfficer(assignedOfficer)
                .status(initialStatus)
                .priorityScore(initialPriority)
                .urgency(request.getUrgency() != null ? request.getUrgency() : Urgency.MEDIUM)
                .anonymous(request.isAnonymous())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .contentHash(contentHash)
                .slaDueAt(slaDueAt)
                .build();

        complaint = complaintRepository.save(complaint);

        // 10. Process Attachments (up to 5 files) & Image Duplicate Check (DUP-4)
        String imageDuplicateMatchCode = null;
        if (files != null && !files.isEmpty()) {
            if (files.size() > 5) {
                throw new BadRequestException("Maximum 5 file uploads allowed per complaint");
            }
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    Attachment attachment = fileStorageService.store(file, complaint, AttachmentKind.EVIDENCE);
                    complaint.getAttachments().add(attachment);

                    // Check for image duplicate in open complaints
                    if (attachment.getSha256() != null) {
                        List<Attachment> shaMatches = attachmentRepository.findOpenAttachmentsBySha256(attachment.getSha256());
                        for (Attachment match : shaMatches) {
                            if (!match.getComplaint().getId().equals(complaint.getId())) {
                                imageDuplicateMatchCode = match.getComplaint().getPublicCode();
                                break;
                            }
                        }
                    }
                    if (imageDuplicateMatchCode == null && attachment.getPhash() != null) {
                        List<Attachment> openAttachments = attachmentRepository.findAllOpenAttachmentsWithPhash();
                        for (Attachment match : openAttachments) {
                            if (!match.getComplaint().getId().equals(complaint.getId()) &&
                                    ImageHashUtil.isVisualDuplicate(attachment.getPhash(), match.getPhash())) {
                                imageDuplicateMatchCode = match.getComplaint().getPublicCode();
                                break;
                            }
                        }
                    }
                }
            }
        }

        // 11. Initial Status History & Follower
        StatusHistory history = StatusHistory.builder()
                .complaint(complaint)
                .fromStatus(null)
                .toStatus(initialStatus)
                .remark(assignedOfficer != null
                        ? "Complaint received and automatically assigned to officer " + assignedOfficer.getName()
                        : "Complaint submitted successfully")
                .changedBy(null) // system
                .changedByName("CivicPulse System")
                .build();
        statusHistoryRepository.save(history);
        complaint.getStatusHistories().add(history);

        // Add citizen as initial follower
        ComplaintFollower follower = ComplaintFollower.builder()
                .complaint(complaint)
                .user(citizen)
                .build();
        complaintFollowerRepository.save(follower);

        // 12. Notifications
        notificationService.createNotification(
                citizen,
                NotificationType.COMPLAINT_SUBMITTED,
                "Complaint Registered: " + publicCode,
                "Your complaint '" + complaint.getTitle() + "' was successfully submitted to " + department.getName() + ".",
                "/citizen/complaint.html?id=" + complaint.getId()
        );

        if (assignedOfficer != null) {
            notificationService.createNotification(
                    assignedOfficer,
                    NotificationType.COMPLAINT_ASSIGNED,
                    "New Complaint Assigned: " + publicCode,
                    "Complaint '" + complaint.getTitle() + "' has been assigned to your queue.",
                    "/officer/workspace.html?id=" + complaint.getId()
            );
        }

        // 13. Audit Log
        auditLogService.log(
                citizen,
                citizen.getEmail(),
                "CREATE_COMPLAINT",
                "Complaint",
                complaint.getId().toString(),
                "Created complaint " + publicCode + " with category " + category.getName(),
                ipAddress
        );

        return enrichResponse(complaint, citizen.getId(), true, imageDuplicateMatchCode);
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long id, User currentUser) {
        Complaint complaint = getEntityById(id);
        boolean isPrivileged = currentUser != null && (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.OFFICER || currentUser.getRole() == Role.DEPT_HEAD);
        Long currentUserId = currentUser != null ? currentUser.getId() : null;

        // Citizen ownership check
        if (currentUser != null && currentUser.getRole() == Role.CITIZEN && !complaint.getCitizen().getId().equals(currentUser.getId())) {
            // Citizens can only view their own complaints on the private /mine endpoints,
            // or view as public if accessing detail
        }

        return enrichResponse(complaint, currentUserId, isPrivileged, null);
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintByPublicCode(String publicCode, User currentUser) {
        Complaint complaint = complaintRepository.findByPublicCode(publicCode)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with code: " + publicCode));

        boolean isPrivileged = currentUser != null && (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.OFFICER || currentUser.getRole() == Role.DEPT_HEAD);
        Long currentUserId = currentUser != null ? currentUser.getId() : null;

        return enrichResponse(complaint, currentUserId, isPrivileged, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getCitizenComplaints(Long citizenId, ComplaintStatus status, String query, Pageable pageable) {
        return complaintRepository.searchCitizenComplaints(citizenId, status, query, pageable)
                .map(c -> enrichResponse(c, citizenId, true, null));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintResponse> getOfficerComplaints(Long departmentId, Long officerId, ComplaintStatus status, Long categoryId, boolean overdueOnly, String query, Pageable pageable) {
        return complaintRepository.searchOfficerComplaints(
                departmentId, officerId, status, categoryId, overdueOnly, Instant.now(), OPEN_STATUSES, query, pageable
        ).map(c -> enrichResponse(c, officerId, true, null));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getKanbanBoardComplaints(Long departmentId, Long officerId) {
        List<Complaint> complaints;
        if (departmentId != null) {
            complaints = complaintRepository.findByDepartmentIdAndStatusIn(departmentId, Arrays.asList(
                    ComplaintStatus.SUBMITTED,
                    ComplaintStatus.ASSIGNED,
                    ComplaintStatus.IN_PROGRESS,
                    ComplaintStatus.RESOLVED,
                    ComplaintStatus.ESCALATED
            ));
        } else {
            complaints = complaintRepository.findAll();
        }

        return complaints.stream()
                .filter(c -> officerId == null || (c.getAssignedOfficer() != null && c.getAssignedOfficer().getId().equals(officerId)))
                .map(c -> enrichResponse(c, officerId, true, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ComplaintResponse changeStatus(Long complaintId, ComplaintStatus newStatus, String remark, User actor, String ipAddress) {
        if (remark == null || remark.trim().isEmpty()) {
            throw new BadRequestException("A remark is mandatory for every status update");
        }

        Complaint complaint = getEntityById(complaintId);
        ComplaintStatus oldStatus = complaint.getStatus();

        if (oldStatus == newStatus) {
            throw new BadRequestException("Complaint is already in status " + newStatus);
        }

        // Validate transition per PRD section 9 transition matrix
        if (!isValidTransition(oldStatus, newStatus, false)) {
            throw new InvalidStatusTransitionException(oldStatus, newStatus);
        }

        // Role ownership validation: officer can only work complaints in their own department
        if (actor != null && actor.getRole() == Role.OFFICER) {
            if (actor.getDepartment() != null && !actor.getDepartment().getId().equals(complaint.getDepartment().getId())) {
                throw new AccessDeniedException("Officers can only update complaints within their own department");
            }
        }

        complaint.setStatus(newStatus);
        if (newStatus == ComplaintStatus.RESOLVED) {
            complaint.setResolvedAt(Instant.now());
        } else if (newStatus == ComplaintStatus.CLOSED) {
            complaint.setClosedAt(Instant.now());
        }

        complaint = complaintRepository.save(complaint);

        // Record history
        StatusHistory history = StatusHistory.builder()
                .complaint(complaint)
                .fromStatus(oldStatus)
                .toStatus(newStatus)
                .remark(remark.trim())
                .changedBy(actor)
                .changedByName(actor != null ? actor.getName() : "System")
                .build();
        statusHistoryRepository.save(history);
        complaint.getStatusHistories().add(history);

        // Notify citizen and all followers
        NotificationType ntfType = switch (newStatus) {
            case RESOLVED -> NotificationType.COMPLAINT_RESOLVED;
            case REOPENED -> NotificationType.COMPLAINT_REOPENED;
            case ESCALATED -> NotificationType.COMPLAINT_ESCALATED;
            default -> NotificationType.STATUS_CHANGED;
        };

        String title = "Complaint " + complaint.getPublicCode() + " is now " + newStatus;
        String msg = "Status updated to " + newStatus + ". Remark: " + remark;

        notificationService.createNotification(
                complaint.getCitizen(),
                ntfType,
                title,
                msg,
                "/citizen/complaint.html?id=" + complaint.getId()
        );

        // Notify other followers (excluding complainant)
        List<ComplaintFollower> followers = complaintFollowerRepository.findByComplaintId(complaint.getId());
        for (ComplaintFollower f : followers) {
            if (!f.getUser().getId().equals(complaint.getCitizen().getId())) {
                notificationService.createNotification(
                        f.getUser(),
                        ntfType,
                        title,
                        msg,
                        "/citizen/complaint.html?id=" + complaint.getId()
                );
            }
        }

        // Audit log
        auditLogService.log(
                actor,
                actor != null ? actor.getEmail() : "system",
                "STATUS_CHANGE",
                "Complaint",
                complaint.getId().toString(),
                oldStatus + " -> " + newStatus + " (" + remark + ")",
                ipAddress
        );

        return enrichResponse(complaint, actor != null ? actor.getId() : null, true, null);
    }

    @Override
    @Transactional
    public ComplaintResponse reassign(Long complaintId, ReassignRequest request, User actor, String ipAddress) {
        Complaint complaint = getEntityById(complaintId);

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BadRequestException("Department not found"));
            complaint.setDepartment(dept);
        }

        if (request.getOfficerId() != null) {
            User newOfficer = userRepository.findById(request.getOfficerId())
                    .orElseThrow(() -> new BadRequestException("Officer not found"));
            complaint.setAssignedOfficer(newOfficer);
        }

        complaint = complaintRepository.save(complaint);

        StatusHistory history = StatusHistory.builder()
                .complaint(complaint)
                .fromStatus(complaint.getStatus())
                .toStatus(complaint.getStatus())
                .remark("Reassigned: " + request.getReason())
                .changedBy(actor)
                .changedByName(actor != null ? actor.getName() : "System")
                .build();
        statusHistoryRepository.save(history);

        auditLogService.log(actor, actor != null ? actor.getEmail() : null, "REASSIGN", "Complaint", complaint.getId().toString(), request.getReason(), ipAddress);

        return enrichResponse(complaint, actor != null ? actor.getId() : null, true, null);
    }

    @Override
    @Transactional
    public ComplaintResponse uploadProof(Long complaintId, MultipartFile file, User officer, String ipAddress) {
        Complaint complaint = getEntityById(complaintId);
        Attachment proof = fileStorageService.store(file, complaint, AttachmentKind.PROOF);
        complaint.getAttachments().add(proof);

        auditLogService.log(officer, officer != null ? officer.getEmail() : null, "UPLOAD_PROOF", "Attachment", proof.getId().toString(), "Proof uploaded for " + complaint.getPublicCode(), ipAddress);

        return enrichResponse(complaint, officer != null ? officer.getId() : null, true, null);
    }

    @Override
    @Transactional
    public ComplaintResponse reopen(Long complaintId, ReopenComplaintRequest request, User citizen, String ipAddress) {
        Complaint complaint = getEntityById(complaintId);

        if (!complaint.getCitizen().getId().equals(citizen.getId())) {
            throw new AccessDeniedException("Only the citizen who filed this complaint can reopen it");
        }

        if (complaint.getStatus() != ComplaintStatus.RESOLVED && complaint.getStatus() != ComplaintStatus.REJECTED) {
            throw new BadRequestException("Only RESOLVED or REJECTED complaints can be reopened");
        }

        // 7-day window check (TRK-6)
        if (complaint.getResolvedAt() != null) {
            long days = Duration.between(complaint.getResolvedAt(), Instant.now()).toDays();
            if (days > 7) {
                throw new BadRequestException("Complaints can only be reopened within 7 days of resolution");
            }
        }

        return changeStatus(complaintId, ComplaintStatus.REOPENED, "Reopened by citizen: " + request.getReason(), citizen, ipAddress);
    }

    @Override
    @Transactional
    public ApiResponse supportComplaint(Long complaintId, User citizen) {
        Complaint complaint = getEntityById(complaintId);

        // Idempotently add follower
        if (!complaintFollowerRepository.existsByComplaintIdAndUserId(complaintId, citizen.getId())) {
            ComplaintFollower follower = ComplaintFollower.builder()
                    .complaint(complaint)
                    .user(citizen)
                    .build();
            complaintFollowerRepository.save(follower);
        }

        // Idempotently add upvote
        if (!upvoteRepository.existsByComplaintIdAndUserId(complaintId, citizen.getId())) {
            Upvote upvote = Upvote.builder()
                    .complaint(complaint)
                    .user(citizen)
                    .build();
            upvoteRepository.save(upvote);
            complaint.setUpvoteCount(complaint.getUpvoteCount() + 1);
            complaint.setPriorityScore(priorityService.computePriority(complaint));
            complaintRepository.save(complaint);
        }

        return ApiResponse.ok("You are now supporting and following complaint " + complaint.getPublicCode());
    }

    @Override
    @Transactional
    public ApiResponse upvoteComplaint(Long complaintId, User citizen) {
        Complaint complaint = getEntityById(complaintId);

        if (upvoteRepository.existsByComplaintIdAndUserId(complaintId, citizen.getId())) {
            return ApiResponse.ok("You have already upvoted this complaint");
        }

        Upvote upvote = Upvote.builder()
                .complaint(complaint)
                .user(citizen)
                .build();
        upvoteRepository.save(upvote);

        complaint.setUpvoteCount(complaint.getUpvoteCount() + 1);
        complaint.setPriorityScore(priorityService.computePriority(complaint));
        complaintRepository.save(complaint);

        return ApiResponse.ok("Upvote recorded successfully");
    }

    @Override
    @Transactional
    public CommentResponse addComment(Long complaintId, CommentRequest request, User author) {
        Complaint complaint = getEntityById(complaintId);

        boolean isStaff = author.getRole() == Role.OFFICER || author.getRole() == Role.DEPT_HEAD || author.getRole() == Role.ADMIN;
        boolean internal = isStaff && request.isInternal();

        Comment comment = Comment.builder()
                .complaint(complaint)
                .author(author)
                .body(request.getBody().trim())
                .internal(internal)
                .build();

        comment = commentRepository.save(comment);

        // Notify other party
        if (!internal) {
            if (isStaff && !complaint.getCitizen().getId().equals(author.getId())) {
                notificationService.createNotification(
                        complaint.getCitizen(),
                        NotificationType.COMMENT_ADDED,
                        "New Comment on " + complaint.getPublicCode(),
                        author.getName() + " commented on your complaint.",
                        "/citizen/complaint.html?id=" + complaint.getId()
                );
            } else if (!isStaff && complaint.getAssignedOfficer() != null) {
                notificationService.createNotification(
                        complaint.getAssignedOfficer(),
                        NotificationType.COMMENT_ADDED,
                        "Citizen Comment on " + complaint.getPublicCode(),
                        "Citizen commented: " + request.getBody(),
                        "/officer/workspace.html?id=" + complaint.getId()
                );
            }
        }

        return CommentResponse.fromEntity(comment);
    }

    @Override
    @Transactional
    public FeedbackResponse submitFeedback(Long complaintId, FeedbackRequest request, User citizen, String ipAddress) {
        Complaint complaint = getEntityById(complaintId);

        if (!complaint.getCitizen().getId().equals(citizen.getId())) {
            throw new AccessDeniedException("Only the citizen who lodged this complaint can submit feedback");
        }

        if (complaint.getStatus() != ComplaintStatus.RESOLVED && complaint.getStatus() != ComplaintStatus.CLOSED) {
            throw new BadRequestException("Feedback can only be submitted after resolution or closure");
        }

        if (feedbackRepository.existsByComplaintId(complaintId)) {
            throw new DuplicateResourceException("Feedback has already been submitted for this complaint");
        }

        Feedback feedback = Feedback.builder()
                .complaint(complaint)
                .rating(request.getRating())
                .comment(request.getComment() != null ? request.getComment().trim() : null)
                .satisfied(request.getSatisfied() != null ? request.getSatisfied() : true)
                .build();

        feedback = feedbackRepository.save(feedback);

        auditLogService.log(
                citizen,
                citizen.getEmail(),
                "FEEDBACK",
                "Feedback",
                feedback.getId().toString(),
                "Rating: " + request.getRating() + " stars for " + complaint.getPublicCode(),
                ipAddress
        );

        return FeedbackResponse.fromEntity(feedback);
    }

    @Override
    @Transactional
    public ComplaintResponse mergeComplaint(Long childId, MergeComplaintRequest request, User officer, String ipAddress) {
        Long parentId = request.getEffectiveParentId();
        if (parentId == null) {
            throw new BadRequestException("Parent complaint ID is required for merging");
        }
        if (childId.equals(parentId)) {
            throw new BadRequestException("A complaint cannot be merged into itself");
        }

        Complaint child = getEntityById(childId);
        Complaint parent = getEntityById(parentId);

        if (parent.getStatus() == ComplaintStatus.MERGED) {
            throw new BadRequestException("Cannot merge into a complaint that is already merged");
        }

        if (!isValidTransition(child.getStatus(), ComplaintStatus.MERGED, false)) {
            throw new InvalidStatusTransitionException(child.getStatus(), ComplaintStatus.MERGED);
        }

        // Set child status to MERGED and link to parent
        ComplaintStatus oldChildStatus = child.getStatus();
        child.setStatus(ComplaintStatus.MERGED);
        child.setDuplicateOf(parent);
        child = complaintRepository.save(child);

        // Move followers from child to parent without duplicate constraint violation
        List<ComplaintFollower> childFollowers = complaintFollowerRepository.findByComplaintId(childId);
        for (ComplaintFollower cf : childFollowers) {
            if (!complaintFollowerRepository.existsByComplaintIdAndUserId(parent.getId(), cf.getUser().getId())) {
                ComplaintFollower newFollower = ComplaintFollower.builder()
                        .complaint(parent)
                        .user(cf.getUser())
                        .build();
                complaintFollowerRepository.save(newFollower);
            }
        }

        // Move upvotes without double counting
        List<Upvote> childUpvotes = upvoteRepository.findByComplaintId(childId);
        int addedUpvotes = 0;
        for (Upvote cu : childUpvotes) {
            if (!upvoteRepository.existsByComplaintIdAndUserId(parent.getId(), cu.getUser().getId())) {
                Upvote newUpvote = Upvote.builder()
                        .complaint(parent)
                        .user(cu.getUser())
                        .build();
                upvoteRepository.save(newUpvote);
                addedUpvotes++;
            }
        }

        if (addedUpvotes > 0) {
            parent.setUpvoteCount(parent.getUpvoteCount() + addedUpvotes);
            parent.setPriorityScore(priorityService.computePriority(parent));
            complaintRepository.save(parent);
        }

        // Add history records
        StatusHistory childHistory = StatusHistory.builder()
                .complaint(child)
                .fromStatus(oldChildStatus)
                .toStatus(ComplaintStatus.MERGED)
                .remark("Merged into parent complaint " + parent.getPublicCode() + ". Reason: " + request.getEffectiveRemark())
                .changedBy(officer)
                .changedByName(officer != null ? officer.getName() : "Officer")
                .build();
        statusHistoryRepository.save(childHistory);

        // Notify both citizens
        notificationService.createNotification(
                child.getCitizen(),
                NotificationType.COMPLAINT_MERGED,
                "Complaint Merged: " + child.getPublicCode(),
                "Your complaint was merged into master complaint " + parent.getPublicCode() + ". You will receive updates directly.",
                "/citizen/complaint.html?id=" + parent.getId()
        );

        notificationService.createNotification(
                parent.getCitizen(),
                NotificationType.STATUS_CHANGED,
                "Support Added: " + parent.getPublicCode(),
                "Another duplicate issue was merged into yours. Upvote count and priority updated.",
                "/citizen/complaint.html?id=" + parent.getId()
        );

        auditLogService.log(
                officer,
                officer != null ? officer.getEmail() : null,
                "MERGE",
                "Complaint",
                childId.toString(),
                "Merged " + child.getPublicCode() + " into " + parent.getPublicCode(),
                ipAddress
        );

        return enrichResponse(child, officer != null ? officer.getId() : null, true, null);
    }

    @Override
    @Transactional
    public ComplaintResponse unmergeComplaint(Long childId, User admin, String ipAddress) {
        Complaint child = getEntityById(childId);
        if (child.getStatus() != ComplaintStatus.MERGED) {
            throw new BadRequestException("Complaint is not currently in MERGED status");
        }

        child.setStatus(ComplaintStatus.ASSIGNED);
        child.setDuplicateOf(null);
        child = complaintRepository.save(child);

        StatusHistory history = StatusHistory.builder()
                .complaint(child)
                .fromStatus(ComplaintStatus.MERGED)
                .toStatus(ComplaintStatus.ASSIGNED)
                .remark("Unmerged by administrator")
                .changedBy(admin)
                .changedByName(admin != null ? admin.getName() : "Administrator")
                .build();
        statusHistoryRepository.save(history);

        auditLogService.log(admin, admin != null ? admin.getEmail() : null, "UNMERGE", "Complaint", childId.toString(), "Unmerged complaint " + child.getPublicCode(), ipAddress);

        return enrichResponse(child, admin != null ? admin.getId() : null, true, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SimilarCandidateResponse> findSimilar(Double lat, Double lng, String text) {
        if (lat == null || lng == null || text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        List<Complaint> openComplaints = complaintRepository.findOpenComplaintsWithLocation(OPEN_STATUSES);
        List<SimilarCandidateResponse> candidates = new ArrayList<>();

        for (Complaint c : openComplaints) {
            double distance = GeoUtils.distanceMeters(lat, lng, c.getLatitude(), c.getLongitude());
            if (distance <= defaultRadiusMeters) {
                double textSim = TextSimilarity.similarity(text, c.getTitle() + " " + c.getDescription());
                if (textSim >= defaultSimilarityThreshold) {
                    candidates.add(SimilarCandidateResponse.builder()
                            .id(c.getId())
                            .publicCode(c.getPublicCode())
                            .title(c.getTitle())
                            .distanceMeters(Math.round(distance))
                            .similarityScore(Math.round(textSim * 100.0) / 100.0)
                            .status(c.getStatus())
                            .upvoteCount(c.getUpvoteCount())
                            .categoryName(c.getCategory() != null ? c.getCategory().getName() : "General")
                            .createdAt(c.getCreatedAt())
                            .build());
                }
            }
        }

        candidates.sort(Comparator.comparingDouble(SimilarCandidateResponse::getDistanceMeters));
        return candidates;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getReceiptPdf(Long complaintId, User currentUser) {
        Complaint complaint = getEntityById(complaintId);
        return pdfReceiptService.generateReceiptPdf(complaint);
    }

    @Override
    public Complaint getEntityById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));
    }

    public static boolean isValidTransition(ComplaintStatus from, ComplaintStatus to, boolean isAdminUnmerge) {
        if (from == to) return true;
        if (isAdminUnmerge && from == ComplaintStatus.MERGED && to == ComplaintStatus.ASSIGNED) {
            return true;
        }
        return switch (from) {
            case SUBMITTED -> to == ComplaintStatus.ASSIGNED || to == ComplaintStatus.REJECTED || to == ComplaintStatus.MERGED;
            case ASSIGNED -> to == ComplaintStatus.IN_PROGRESS || to == ComplaintStatus.REJECTED || to == ComplaintStatus.ESCALATED || to == ComplaintStatus.MERGED;
            case IN_PROGRESS -> to == ComplaintStatus.RESOLVED || to == ComplaintStatus.ESCALATED || to == ComplaintStatus.MERGED;
            case ESCALATED -> to == ComplaintStatus.IN_PROGRESS || to == ComplaintStatus.RESOLVED || to == ComplaintStatus.MERGED;
            case RESOLVED -> to == ComplaintStatus.CLOSED || to == ComplaintStatus.REOPENED;
            case REJECTED -> to == ComplaintStatus.REOPENED;
            case REOPENED -> to == ComplaintStatus.IN_PROGRESS || to == ComplaintStatus.RESOLVED;
            case MERGED -> false;
            case CLOSED -> false;
        };
    }

    private ComplaintResponse enrichResponse(Complaint c, Long currentUserId, boolean isPrivileged, String imageDuplicateMatch) {
        ComplaintResponse resp = ComplaintResponse.fromEntity(c, currentUserId, isPrivileged);

        // Populate attachments
        List<AttachmentResponse> attList = new ArrayList<>();
        if (c.getAttachments() != null) {
            for (Attachment a : c.getAttachments()) {
                AttachmentResponse ar = AttachmentResponse.fromEntity(a);
                if (imageDuplicateMatch != null) {
                    ar.setImageDuplicateOf(imageDuplicateMatch);
                }
                attList.add(ar);
            }
        }
        resp.setAttachments(attList);

        // Populate status histories
        if (c.getStatusHistories() != null) {
            resp.setStatusHistories(c.getStatusHistories().stream()
                    .map(StatusHistoryResponse::fromEntity)
                    .sorted(Comparator.comparing(StatusHistoryResponse::getCreatedAt))
                    .collect(Collectors.toList()));
        }

        // Populate comments (filter internal comments if user is not staff)
        if (c.getComments() != null) {
            resp.setComments(c.getComments().stream()
                    .filter(cm -> isPrivileged || !cm.isInternal())
                    .map(CommentResponse::fromEntity)
                    .sorted(Comparator.comparing(CommentResponse::getCreatedAt))
                    .collect(Collectors.toList()));
        }

        // Populate feedback
        Optional<Feedback> fbOpt = feedbackRepository.findByComplaintId(c.getId());
        fbOpt.ifPresent(feedback -> resp.setFeedback(FeedbackResponse.fromEntity(feedback)));

        // Upvote and follower flags
        if (currentUserId != null) {
            resp.setHasUpvoted(upvoteRepository.existsByComplaintIdAndUserId(c.getId(), currentUserId));
            resp.setFollowing(complaintFollowerRepository.existsByComplaintIdAndUserId(c.getId(), currentUserId));
        }

        return resp;
    }
}
