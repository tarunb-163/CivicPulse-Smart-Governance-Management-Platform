package com.civicpulse.repository;

import com.civicpulse.entity.Complaint;
import com.civicpulse.enums.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByPublicCode(String publicCode);

    Page<Complaint> findByCitizenId(Long citizenId, Pageable pageable);

    @Query("SELECT c FROM Complaint c WHERE c.citizen.id = :citizenId AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:query IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.publicCode) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Complaint> searchCitizenComplaints(@Param("citizenId") Long citizenId,
                                            @Param("status") ComplaintStatus status,
                                            @Param("query") String query,
                                            Pageable pageable);

    @Query("SELECT c FROM Complaint c WHERE " +
           "(:departmentId IS NULL OR c.department.id = :departmentId) AND " +
           "(:officerId IS NULL OR (c.assignedOfficer IS NOT NULL AND c.assignedOfficer.id = :officerId)) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:categoryId IS NULL OR c.category.id = :categoryId) AND " +
           "(:overdueOnly = false OR (c.slaDueAt IS NOT NULL AND c.slaDueAt < :now AND c.status IN :openStatuses)) AND " +
           "(:query IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.publicCode) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.address) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Complaint> searchOfficerComplaints(@Param("departmentId") Long departmentId,
                                            @Param("officerId") Long officerId,
                                            @Param("status") ComplaintStatus status,
                                            @Param("categoryId") Long categoryId,
                                            @Param("overdueOnly") boolean overdueOnly,
                                            @Param("now") Instant now,
                                            @Param("openStatuses") Collection<ComplaintStatus> openStatuses,
                                            @Param("query") String query,
                                            Pageable pageable);

    List<Complaint> findByDepartmentIdAndStatusIn(Long departmentId, Collection<ComplaintStatus> statuses);

    // Exact duplicate check
    List<Complaint> findByCitizenIdAndContentHashAndCreatedAtAfter(Long citizenId, String contentHash, Instant after);

    // Open complaints for nearby check
    @Query("SELECT c FROM Complaint c WHERE c.status IN :openStatuses AND c.latitude IS NOT NULL AND c.longitude IS NOT NULL")
    List<Complaint> findOpenComplaintsWithLocation(@Param("openStatuses") Collection<ComplaintStatus> openStatuses);

    // Rate limiting count
    long countByCitizenIdAndCreatedAtAfter(Long citizenId, Instant after);

    // Sla scheduler
    @Query("SELECT c FROM Complaint c LEFT JOIN FETCH c.citizen LEFT JOIN FETCH c.department WHERE c.status IN :statuses AND c.slaDueAt < :now")
    List<Complaint> findByStatusInAndSlaDueAtBefore(@Param("statuses") Collection<ComplaintStatus> statuses, @Param("now") Instant now);

    // AutoClose scheduler
    @Query("SELECT c FROM Complaint c LEFT JOIN FETCH c.citizen WHERE c.status = :status AND c.resolvedAt < :cutoff")
    List<Complaint> findByStatusAndResolvedAtBefore(@Param("status") ComplaintStatus status, @Param("cutoff") Instant cutoff);

    // Officer workload count (assigned open complaints)
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.assignedOfficer.id = :officerId AND c.status IN :openStatuses")
    long countActiveComplaintsByOfficer(@Param("officerId") Long officerId, @Param("openStatuses") Collection<ComplaintStatus> openStatuses);

    // Analytics: count by status
    long countByStatus(ComplaintStatus status);
    long countByStatusIn(Collection<ComplaintStatus> statuses);

    @Query("SELECT c.status, COUNT(c) FROM Complaint c GROUP BY c.status")
    List<Object[]> countGroupByStatus();

    @Query("SELECT c.department.id, c.department.name, COUNT(c) FROM Complaint c GROUP BY c.department.id, c.department.name")
    List<Object[]> countGroupByDepartment();

    @Query("SELECT c.category.id, c.category.name, c.department.name, COUNT(c) FROM Complaint c GROUP BY c.category.id, c.category.name, c.department.name")
    List<Object[]> countGroupByCategory();

    @Query("SELECT c.latitude, c.longitude, c.urgency, c.status FROM Complaint c WHERE c.latitude IS NOT NULL AND c.longitude IS NOT NULL")
    List<Object[]> findHeatmapPoints();

    // Sequence helper for code generation (count within a given year)
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.createdAt >= :yearStart AND c.createdAt < :yearEnd")
    long countComplaintsInYear(@Param("yearStart") Instant yearStart, @Param("yearEnd") Instant yearEnd);

    // Public transparency statistics
    @Query("SELECT COUNT(c) FROM Complaint c WHERE c.status = 'RESOLVED' OR c.status = 'CLOSED'")
    long countResolvedComplaints();
}
