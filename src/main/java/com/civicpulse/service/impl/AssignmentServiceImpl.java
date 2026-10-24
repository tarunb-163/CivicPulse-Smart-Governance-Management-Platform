package com.civicpulse.service.impl;

import com.civicpulse.entity.Department;
import com.civicpulse.entity.User;
import com.civicpulse.enums.ComplaintStatus;
import com.civicpulse.enums.Role;
import com.civicpulse.repository.ComplaintRepository;
import com.civicpulse.repository.UserRepository;
import com.civicpulse.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final UserRepository userRepository;
    private final ComplaintRepository complaintRepository;

    private static final List<ComplaintStatus> ACTIVE_STATUSES = Arrays.asList(
            ComplaintStatus.SUBMITTED,
            ComplaintStatus.ASSIGNED,
            ComplaintStatus.IN_PROGRESS,
            ComplaintStatus.ESCALATED,
            ComplaintStatus.REOPENED
    );

    @Override
    @Transactional(readOnly = true)
    public User assignLeastLoadedOfficer(Department department) {
        if (department == null) return null;

        // Find active officers in this department
        List<User> officers = userRepository.findByDepartmentIdAndRoleAndEnabledTrue(department.getId(), Role.OFFICER);

        if (officers.isEmpty()) {
            // Check if there is a department head or any officer
            officers = userRepository.findByDepartmentIdAndRoleAndEnabledTrue(department.getId(), Role.DEPT_HEAD);
        }

        if (officers.isEmpty()) {
            // Fallback: check if headUser is set
            if (department.getHeadUser() != null && department.getHeadUser().isEnabled()) {
                return department.getHeadUser();
            }
            // Fallback to any active officer in the system
            officers = userRepository.findByRoleAndEnabledTrue(Role.OFFICER);
        }

        if (officers.isEmpty()) {
            log.warn("No active officers found to assign complaint for department: {}", department.getName());
            return null;
        }

        // Find the officer with lowest count of active open complaints
        return officers.stream()
                .min(Comparator.comparingLong(officer -> complaintRepository.countActiveComplaintsByOfficer(officer.getId(), ACTIVE_STATUSES)))
                .orElse(officers.get(0));
    }
}
