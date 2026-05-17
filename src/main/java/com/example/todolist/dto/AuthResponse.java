package com.example.todolist.dto;

public class AuthResponse {

    private String username;
    private String tenantId;
    private String token;

    public AuthResponse(String username, String tenantId, String token) {
        this.username = username;
        this.tenantId = tenantId;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getToken() {
        return token;
    }
}
