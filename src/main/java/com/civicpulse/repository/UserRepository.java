package com.civicpulse.repository;

import com.civicpulse.entity.User;
import com.civicpulse.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    List<User> findByDepartmentIdAndRoleAndEnabledTrue(Long departmentId, Role role);
    List<User> findByRoleAndEnabledTrue(Role role);

    @Query("SELECT u FROM User u WHERE " +
           "(:query IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:departmentId IS NULL OR (u.department IS NOT NULL AND u.department.id = :departmentId))")
    Page<User> searchUsers(@Param("query") String query,
                           @Param("role") Role role,
                           @Param("departmentId") Long departmentId,
                           Pageable pageable);

    long countByRole(Role role);
}
