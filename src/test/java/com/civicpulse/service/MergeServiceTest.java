package com.civicpulse.service;

import com.civicpulse.dto.request.MergeComplaintRequest;
import com.civicpulse.dto.response.ComplaintResponse;
import com.civicpulse.entity.Complaint;
import com.civicpulse.entity.Upvote;
import com.civicpulse.entity.User;
import com.civicpulse.enums.ComplaintStatus;
import com.civicpulse.enums.Role;
import com.civicpulse.repository.*;
import com.civicpulse.service.impl.ComplaintServiceImpl;
import com.civicpulse.util.CodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MergeServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private StatusHistoryRepository statusHistoryRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UpvoteRepository upvoteRepository;

    @Mock
    private ComplaintFollowerRepository complaintFollowerRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private SystemSettingRepository systemSettingRepository;

    @Mock
    private ClassifierService classifierService;

    @Mock
    private PriorityService priorityService;

    @Mock
    private AssignmentService assignmentService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private PdfReceiptService pdfReceiptService;

    @InjectMocks
    private ComplaintServiceImpl complaintService;

    private User officer;
    private User citizen1;
    private User citizen2;
    private Complaint parent;
    private Complaint child;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        officer = User.builder().id(201L).name("Officer Kumar").role(Role.OFFICER).build();
        citizen1 = User.builder().id(101L).name("Citizen One").email("c1@example.com").build();
        citizen2 = User.builder().id(102L).name("Citizen Two").email("c2@example.com").build();

        parent = Complaint.builder()
                .id(1L)
                .publicCode("CP-2026-000001")
                .title("Huge pothole on Main Road")
                .status(ComplaintStatus.IN_PROGRESS)
                .citizen(citizen1)
                .upvoteCount(2)
                .priorityScore(15.0)
                .build();

        child = Complaint.builder()
                .id(2L)
                .publicCode("CP-2026-000002")
                .title("Road damaged and crater near junction")
                .status(ComplaintStatus.SUBMITTED)
                .citizen(citizen2)
                .upvoteCount(1)
                .priorityScore(12.0)
                .build();

        when(complaintRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(complaintRepository.findById(2L)).thenReturn(Optional.of(child));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    @DisplayName("Officer merges duplicate complaint into parent; status changes to MERGED and upvotes migrate")
    void testMergeComplaints() {
        MergeComplaintRequest req = MergeComplaintRequest.builder()
                .parentId(1L)
                .remark("Identical pothole reported within 50m")
                .build();

        when(complaintFollowerRepository.findByComplaintId(2L)).thenReturn(Collections.emptyList());
        when(upvoteRepository.findByComplaintId(2L)).thenReturn(List.of(
                Upvote.builder().id(99L).complaint(child).user(citizen2).build()
        ));
        when(upvoteRepository.existsByComplaintIdAndUserId(1L, 102L)).thenReturn(false);
        when(priorityService.computePriority(any(Complaint.class))).thenReturn(17.5);

        ComplaintResponse response = complaintService.mergeComplaint(2L, req, officer, "127.0.0.1");

        assertNotNull(response);
        assertEquals(ComplaintStatus.MERGED, child.getStatus());
        assertEquals(parent, child.getDuplicateOf());
        assertEquals(3, parent.getUpvoteCount());

        verify(statusHistoryRepository).save(any());
        verify(auditLogService).log(any(), any(), any(), any(), any(), any(), any());
    }
}
