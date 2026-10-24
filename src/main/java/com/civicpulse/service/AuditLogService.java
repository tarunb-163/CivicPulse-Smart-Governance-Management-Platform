package com.civicpulse.service;

import com.civicpulse.entity.User;

public interface AuditLogService {
    void log(User actor, String actorEmail, String action, String entity, String entityId, String details, String ip);
}
