package com.civicpulse.repository;

import com.civicpulse.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByActiveTrueOrderByCreatedAtDesc();

    @Query("SELECT a FROM Announcement a WHERE a.active = true AND " +
           "(a.startsAt IS NULL OR a.startsAt <= :now) AND " +
           "(a.endsAt IS NULL OR a.endsAt >= :now) ORDER BY a.createdAt DESC")
    List<Announcement> findCurrentlyActive(@Param("now") Instant now);
}
