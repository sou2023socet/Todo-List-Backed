package com.example.todolist.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final int status_code;
    private final String message;
    private final T data;
    private final String timestamp;

    public ApiResponse(int status_code, String message, T data, String timestamp) {
        this.status_code = status_code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(200, message, data, java.time.Instant.now().toString());
    }

    public static <T> ApiResponse<T> success(T data, String message, String timestamp) {
        return new ApiResponse<>(200, message, data, timestamp);
    }

    public static <T> ApiResponse<T> error(int statusCode, String message, T data, String timestamp) {
        return new ApiResponse<>(statusCode, message, data, timestamp);
    }

    public int getStatus_code() {
        return status_code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
