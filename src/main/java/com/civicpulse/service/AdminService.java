package com.civicpulse.service;

import com.civicpulse.dto.request.*;
import com.civicpulse.dto.response.*;
import com.civicpulse.entity.SystemSetting;
import com.civicpulse.entity.User;
import com.civicpulse.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {
    List<DepartmentResponse> getAllDepartments();
    DepartmentResponse getDepartmentById(Long id);
    DepartmentResponse createDepartment(DepartmentRequest request, User admin, String ip);
    DepartmentResponse updateDepartment(Long id, DepartmentRequest request, User admin, String ip);
    ApiResponse deleteDepartment(Long id, User admin, String ip);

    List<CategoryResponse> getAllCategories(Long departmentId);
    CategoryResponse getCategoryById(Long id);
    CategoryResponse createCategory(CategoryRequest request, User admin, String ip);
    CategoryResponse updateCategory(Long id, CategoryRequest request, User admin, String ip);
    ApiResponse deleteCategory(Long id, User admin, String ip);

    List<KeywordRuleResponse> getAllKeywordRules(Long categoryId);
    KeywordRuleResponse createKeywordRule(KeywordRuleRequest request, User admin, String ip);
    KeywordRuleResponse updateKeywordRule(Long id, KeywordRuleRequest request, User admin, String ip);
    ApiResponse deleteKeywordRule(Long id, User admin, String ip);
    CategorySuggestion testKeywordRules(String text);

    Page<UserResponse> getUsers(String query, Role role, Long departmentId, Pageable pageable);
    UserResponse createOfficer(CreateOfficerRequest request, User admin, String ip);
    UserResponse updateUser(Long id, UpdateProfileRequest request, User admin, String ip);
    UserResponse toggleUserStatus(Long id, boolean enabled, User admin, String ip);
    UserResponse changeUserRole(Long id, Role role, Long departmentId, User admin, String ip);

    CategoryResponse updateSlaPolicy(Long categoryId, SlaPolicyRequest request, User admin, String ip);

    List<AnnouncementResponse> getAnnouncements();
    AnnouncementResponse createAnnouncement(AnnouncementRequest request, User admin, String ip);
    ApiResponse deleteAnnouncement(Long id, User admin, String ip);

    List<SystemSetting> getAllSettings();
    SystemSetting updateSetting(SystemSettingRequest request, User admin, String ip);

    Page<AuditLogResponse> getAuditLogs(String action, String entity, String query, Pageable pageable);
    Page<DevOutboxResponse> getDevOutbox(Pageable pageable);
}
