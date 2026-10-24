package com.civicpulse.repository;

import com.civicpulse.entity.DevOutbox;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevOutboxRepository extends JpaRepository<DevOutbox, Long> {
    List<DevOutbox> findByRecipientEmailOrderByCreatedAtDesc(String email);
    Page<DevOutbox> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
