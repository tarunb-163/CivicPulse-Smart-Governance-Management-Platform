package com.civicpulse.service;

import com.civicpulse.entity.Department;
import com.civicpulse.entity.User;

public interface AssignmentService {
    User assignLeastLoadedOfficer(Department department);
}
