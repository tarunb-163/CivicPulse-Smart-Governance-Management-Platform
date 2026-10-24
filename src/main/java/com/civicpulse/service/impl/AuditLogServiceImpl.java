package com.civicpulse.service.impl;

import com.civicpulse.entity.AuditLog;
import com.civicpulse.entity.User;
import com.civicpulse.repository.AuditLogRepository;
import com.civicpulse.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final com.civicpulse.repository.UserRepository userRepository;

    @Async("taskExecutor")
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User actor, String actorEmail, String action, String entity, String entityId, String details, String ip) {
        try {
            User safeActor = null;
            if (actor != null && actor.getId() != null) {
                safeActor = userRepository.findById(actor.getId()).orElse(null);
            }
            AuditLog auditLog = AuditLog.builder()
                    .actor(safeActor)
                    .actorEmail(actorEmail != null ? actorEmail : (safeActor != null ? safeActor.getEmail() : (actor != null ? actor.getEmail() : "anonymous")))
                    .action(action)
                    .entity(entity)
                    .entityId(entityId)
                    .details(details)
                    .ip(ip)
                    .build();
            auditLogRepository.save(auditLog);
            log.info("AUDIT: action={} entity={}:{} by={} ip={}", action, entity, entityId, auditLog.getActorEmail(), ip);
        } catch (Exception e) {
            log.error("Failed to write audit log: {}", e.getMessage(), e);
        }
    }
}
