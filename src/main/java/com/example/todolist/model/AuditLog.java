package com.example.todolist.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;
    private String username;
    private String tenantId;
    private String operation;
    private String entity;
    private String entityId;
    private String detail;
    private Instant timestamp;

    public AuditLog() {
    }

    public AuditLog(String username, String tenantId, String operation, String entity, String entityId, String detail, Instant timestamp) {
        this.username = username;
        this.tenantId = tenantId;
        this.operation = operation;
        this.entity = entity;
        this.entityId = entityId;
        this.detail = detail;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getOperation() {
        return operation;
    }

    public String getEntity() {
        return entity;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getDetail() {
        return detail;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
