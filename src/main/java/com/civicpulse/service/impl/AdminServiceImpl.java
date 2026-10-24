package com.civicpulse.service.impl;

import com.civicpulse.dto.request.*;
import com.civicpulse.dto.response.*;
import com.civicpulse.entity.*;
import com.civicpulse.enums.Role;
import com.civicpulse.exception.BadRequestException;
import com.civicpulse.exception.DuplicateResourceException;
import com.civicpulse.exception.ResourceNotFoundException;
import com.civicpulse.repository.*;
import com.civicpulse.service.AdminService;
import com.civicpulse.service.AuditLogService;
import com.civicpulse.service.ClassifierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final DepartmentRepository departmentRepository;
    private final CategoryRepository categoryRepository;
    private final KeywordRuleRepository keywordRuleRepository;
    private final UserRepository userRepository;
    private final AnnouncementRepository announcementRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final AuditLogRepository auditLogRepository;
    private final DevOutboxRepository devOutboxRepository;

    private final ClassifierService classifierService;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(DepartmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return DepartmentResponse.fromEntity(dept);
    }

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request, User admin, String ip) {
        if (departmentRepository.existsByCode(request.getCode().trim().toUpperCase())) {
            throw new DuplicateResourceException("Department code already exists: " + request.getCode());
        }
        if (departmentRepository.existsByName(request.getName().trim())) {
            throw new DuplicateResourceException("Department name already exists: " + request.getName());
        }

        User headUser = null;
        if (request.getHeadUserId() != null) {
            headUser = userRepository.findById(request.getHeadUserId()).orElse(null);
        }

        Department dept = Department.builder()
                .name(request.getName().trim())
                .code(request.getCode().trim().toUpperCase())
                .contactEmail(request.getContactEmail())
                .defaultSlaHours(request.getDefaultSlaHours() != null ? request.getDefaultSlaHours() : 72)
                .active(request.getActive() != null ? request.getActive() : true)
                .headUser(headUser)
                .build();

        dept = departmentRepository.save(dept);
        auditLogService.log(admin, admin.getEmail(), "CREATE_DEPARTMENT", "Department", dept.getId().toString(), "Created " + dept.getName(), ip);
        return DepartmentResponse.fromEntity(dept);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request, User admin, String ip) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        dept.setName(request.getName().trim());
        dept.setCode(request.getCode().trim().toUpperCase());
        if (request.getContactEmail() != null) dept.setContactEmail(request.getContactEmail().trim());
        if (request.getDefaultSlaHours() != null) dept.setDefaultSlaHours(request.getDefaultSlaHours());
        if (request.getActive() != null) dept.setActive(request.getActive());

        if (request.getHeadUserId() != null) {
            User head = userRepository.findById(request.getHeadUserId()).orElse(null);
            dept.setHeadUser(head);
        }

        dept = departmentRepository.save(dept);
        auditLogService.log(admin, admin.getEmail(), "UPDATE_DEPARTMENT", "Department", dept.getId().toString(), "Updated " + dept.getName(), ip);
        return DepartmentResponse.fromEntity(dept);
    }

    @Override
    @Transactional
    public ApiResponse deleteDepartment(Long id, User admin, String ip) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        dept.setActive(false);
        departmentRepository.save(dept);
        auditLogService.log(admin, admin.getEmail(), "DEACTIVATE_DEPARTMENT", "Department", id.toString(), "Deactivated " + dept.getName(), ip);
        return ApiResponse.ok("Department deactivated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories(Long departmentId) {
        List<Category> list = (departmentId != null)
                ? categoryRepository.findByDepartmentIdAndActiveTrue(departmentId)
                : categoryRepository.findAll();
        return list.stream().map(CategoryResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return CategoryResponse.fromEntity(cat);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, User admin, String ip) {
        Department dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BadRequestException("Invalid department ID: " + request.getDepartmentId()));

        Category cat = Category.builder()
                .name(request.getName().trim())
                .department(dept)
                .severityWeight(request.getSeverityWeight() != null ? request.getSeverityWeight() : 3)
                .defaultSlaHours(request.getDefaultSlaHours() != null ? request.getDefaultSlaHours() : dept.getDefaultSlaHours())
                .icon(request.getIcon() != null ? request.getIcon().trim() : "bi-tag")
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        cat = categoryRepository.save(cat);
        auditLogService.log(admin, admin.getEmail(), "CREATE_CATEGORY", "Category", cat.getId().toString(), "Created " + cat.getName(), ip);
        return CategoryResponse.fromEntity(cat);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request, User admin, String ip) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (request.getName() != null) cat.setName(request.getName().trim());
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new BadRequestException("Department not found"));
            cat.setDepartment(dept);
        }
        if (request.getSeverityWeight() != null) cat.setSeverityWeight(request.getSeverityWeight());
        if (request.getDefaultSlaHours() != null) cat.setDefaultSlaHours(request.getDefaultSlaHours());
        if (request.getIcon() != null) cat.setIcon(request.getIcon().trim());
        if (request.getActive() != null) cat.setActive(request.getActive());

        cat = categoryRepository.save(cat);
        auditLogService.log(admin, admin.getEmail(), "UPDATE_CATEGORY", "Category", cat.getId().toString(), "Updated " + cat.getName(), ip);
        return CategoryResponse.fromEntity(cat);
    }

    @Override
    @Transactional
    public ApiResponse deleteCategory(Long id, User admin, String ip) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        cat.setActive(false);
        categoryRepository.save(cat);
        auditLogService.log(admin, admin.getEmail(), "DEACTIVATE_CATEGORY", "Category", id.toString(), "Deactivated " + cat.getName(), ip);
        return ApiResponse.ok("Category deactivated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<KeywordRuleResponse> getAllKeywordRules(Long categoryId) {
        List<KeywordRule> list = (categoryId != null)
                ? keywordRuleRepository.findByCategoryId(categoryId)
                : keywordRuleRepository.findAllWithCategoryAndDepartment();
        return list.stream().map(KeywordRuleResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public KeywordRuleResponse createKeywordRule(KeywordRuleRequest request, User admin, String ip) {
        Category cat = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Category not found with id: " + request.getCategoryId()));

        KeywordRule rule = KeywordRule.builder()
                .keyword(request.getKeyword().trim().toLowerCase())
                .category(cat)
                .weight(request.getWeight() > 0 ? request.getWeight() : 1)
                .build();

        rule = keywordRuleRepository.save(rule);
        auditLogService.log(admin, admin.getEmail(), "CREATE_RULE", "KeywordRule", rule.getId().toString(), "Added keyword '" + rule.getKeyword() + "' -> " + cat.getName(), ip);
        return KeywordRuleResponse.fromEntity(rule);
    }

    @Override
    @Transactional
    public KeywordRuleResponse updateKeywordRule(Long id, KeywordRuleRequest request, User admin, String ip) {
        KeywordRule rule = keywordRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Keyword rule not found with id: " + id));

        Category cat = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BadRequestException("Category not found"));

        rule.setKeyword(request.getKeyword().trim().toLowerCase());
        rule.setCategory(cat);
        rule.setWeight(request.getWeight() > 0 ? request.getWeight() : 1);

        rule = keywordRuleRepository.save(rule);
        auditLogService.log(admin, admin.getEmail(), "UPDATE_RULE", "KeywordRule", rule.getId().toString(), "Updated rule " + rule.getKeyword(), ip);
        return KeywordRuleResponse.fromEntity(rule);
    }

    @Override
    @Transactional
    public ApiResponse deleteKeywordRule(Long id, User admin, String ip) {
        KeywordRule rule = keywordRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Keyword rule not found with id: " + id));
        keywordRuleRepository.delete(rule);
        auditLogService.log(admin, admin.getEmail(), "DELETE_RULE", "KeywordRule", id.toString(), "Deleted rule " + rule.getKeyword(), ip);
        return ApiResponse.ok("Keyword rule deleted successfully");
    }

    @Override
    public CategorySuggestion testKeywordRules(String text) {
        return classifierService.classify(text);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(String query, Role role, Long departmentId, Pageable pageable) {
        return userRepository.searchUsers(query, role, departmentId, pageable)
                .map(UserResponse::fromEntity);
    }

    @Override
    @Transactional
    public UserResponse createOfficer(CreateOfficerRequest request, User admin, String ip) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User already exists with email: " + email);
        }

        Department dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BadRequestException("Department not found with id: " + request.getDepartmentId()));

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.OFFICER)
                .department(dept)
                .enabled(true)
                .emailVerified(true)
                .locale("en")
                .consentVersion("1.0")
                .consentAt(Instant.now())
                .build();

        user = userRepository.save(user);
        auditLogService.log(admin, admin.getEmail(), "CREATE_OFFICER", "User", user.getId().toString(), "Created staff " + user.getEmail() + " (" + user.getRole() + ") in " + dept.getName(), ip);
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateProfileRequest request, User admin, String ip) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getName() != null) user.setName(request.getName().trim());
        if (request.getPhone() != null) user.setPhone(request.getPhone().trim());
        if (request.getLocale() != null) user.setLocale(request.getLocale().trim());

        user = userRepository.save(user);
        auditLogService.log(admin, admin.getEmail(), "UPDATE_USER", "User", user.getId().toString(), "Admin updated user details", ip);
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponse toggleUserStatus(Long id, boolean enabled, User admin, String ip) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setEnabled(enabled);
        user = userRepository.save(user);
        auditLogService.log(admin, admin.getEmail(), enabled ? "ENABLE_USER" : "DISABLE_USER", "User", user.getId().toString(), "Status changed to " + enabled, ip);
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponse changeUserRole(Long id, Role role, Long departmentId, User admin, String ip) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setRole(role);
        if (departmentId != null) {
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new BadRequestException("Department not found"));
            user.setDepartment(dept);
        } else if (role == Role.CITIZEN || role == Role.ADMIN) {
            user.setDepartment(null);
        }

        user = userRepository.save(user);
        auditLogService.log(admin, admin.getEmail(), "ROLE_CHANGE", "User", user.getId().toString(), "Role changed to " + role, ip);
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public CategoryResponse updateSlaPolicy(Long categoryId, SlaPolicyRequest request, User admin, String ip) {
        Category cat = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));

        cat.setDefaultSlaHours(request.getDefaultSlaHours());
        if (request.getSeverityWeight() != null) {
            cat.setSeverityWeight(request.getSeverityWeight());
        }

        cat = categoryRepository.save(cat);
        auditLogService.log(admin, admin.getEmail(), "UPDATE_SLA_POLICY", "Category", categoryId.toString(), "New SLA: " + request.getDefaultSlaHours() + "h", ip);
        return CategoryResponse.fromEntity(cat);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementResponse> getAnnouncements() {
        return announcementRepository.findByActiveTrueOrderByCreatedAtDesc().stream()
                .map(AnnouncementResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AnnouncementResponse createAnnouncement(AnnouncementRequest request, User admin, String ip) {
        Announcement announcement = Announcement.builder()
                .title(request.getTitle().trim())
                .body(request.getBody().trim())
                .active(request.isActive())
                .startsAt(request.getStartsAt() != null ? request.getStartsAt() : Instant.now())
                .endsAt(request.getEndsAt())
                .build();

        announcement = announcementRepository.save(announcement);
        auditLogService.log(admin, admin.getEmail(), "CREATE_ANNOUNCEMENT", "Announcement", announcement.getId().toString(), announcement.getTitle(), ip);
        return AnnouncementResponse.fromEntity(announcement);
    }

    @Override
    @Transactional
    public ApiResponse deleteAnnouncement(Long id, User admin, String ip) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found with id: " + id));
        announcementRepository.delete(announcement);
        auditLogService.log(admin, admin.getEmail(), "DELETE_ANNOUNCEMENT", "Announcement", id.toString(), announcement.getTitle(), ip);
        return ApiResponse.ok("Announcement deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemSetting> getAllSettings() {
        return systemSettingRepository.findAll();
    }

    @Override
    @Transactional
    public SystemSetting updateSetting(SystemSettingRequest request, User admin, String ip) {
        SystemSetting setting = systemSettingRepository.findById(request.getSettingKey())
                .orElse(SystemSetting.builder()
                        .settingKey(request.getSettingKey())
                        .build());

        setting.setSettingValue(request.getSettingValue().trim());
        if (request.getDescription() != null) setting.setDescription(request.getDescription().trim());
        setting = systemSettingRepository.save(setting);

        auditLogService.log(admin, admin.getEmail(), "UPDATE_SETTING", "SystemSetting", setting.getSettingKey(), setting.getSettingValue(), ip);
        return setting;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(String action, String entity, String query, Pageable pageable) {
        return auditLogRepository.searchAuditLogs(action, entity, query, pageable)
                .map(AuditLogResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DevOutboxResponse> getDevOutbox(Pageable pageable) {
        return devOutboxRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(DevOutboxResponse::fromEntity);
    }
}
