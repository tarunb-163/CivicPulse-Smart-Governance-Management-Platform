package com.civicpulse.dto.response;

import com.civicpulse.entity.User;
import com.civicpulse.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private boolean enabled;
    private boolean emailVerified;
    private String locale;
    private boolean emailNotifications;
    private boolean inAppNotifications;
    private String consentVersion;
    private Instant consentAt;
    private Instant createdAt;

    public static UserResponse fromEntity(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .departmentId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                .departmentCode(user.getDepartment() != null ? user.getDepartment().getCode() : null)
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .locale(user.getLocale())
                .emailNotifications(user.isEmailNotifications())
                .inAppNotifications(user.isInAppNotifications())
                .consentVersion(user.getConsentVersion())
                .consentAt(user.getConsentAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
