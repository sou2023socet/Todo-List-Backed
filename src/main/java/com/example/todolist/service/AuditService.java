package com.example.todolist.service;

import com.example.todolist.model.AuditLog;
import com.example.todolist.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String username, String tenantId, String operation, String entity, String entityId, String detail) {
        AuditLog auditLog = new AuditLog(username, tenantId, operation, entity, entityId, detail, Instant.now());
        auditLogRepository.save(auditLog);
    }
}
