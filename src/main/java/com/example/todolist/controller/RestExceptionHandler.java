package com.example.todolist.controller;

import com.example.todolist.dto.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::fieldErrorToMap)
                .collect(Collectors.toList());

        Map<String, Object> payload = Map.of("errors", errors);
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "Validation failed", payload, Instant.now().toString()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(400, "Malformed JSON request", Map.of("error", ex.getMostSpecificCause().getMessage()), Instant.now().toString()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(401).body(ApiResponse.error(401, "Invalid credentials", Map.of("error", ex.getMessage()), Instant.now().toString()));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleExpiredJwt(ExpiredJwtException ex) {
        return ResponseEntity.status(401).body(ApiResponse.error(401, "Expired token", Map.of("error", ex.getMessage()), Instant.now().toString()));
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleJwtException(JwtException ex) {
        return ResponseEntity.status(401).body(ApiResponse.error(401, "Invalid token", Map.of("error", ex.getMessage()), Instant.now().toString()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(400, ex.getMessage(), Map.of("error", ex.getMessage()), Instant.now().toString()));
    }

    private Map<String, String> fieldErrorToMap(FieldError fieldError) {
        return Map.of(
                "field", fieldError.getField(),
                "message", fieldError.getDefaultMessage()
        );
    }
}
