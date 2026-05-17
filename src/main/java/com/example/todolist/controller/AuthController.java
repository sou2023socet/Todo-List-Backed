package com.example.todolist.controller;

import com.example.todolist.dto.ApiResponse;
import com.example.todolist.dto.AuthResponse;
import com.example.todolist.dto.LoginRequest;
import com.example.todolist.dto.RegisterRequest;
import com.example.todolist.model.UserAccount;
import com.example.todolist.security.JwtTokenProvider;
import com.example.todolist.service.AuthService;
import com.example.todolist.service.LoginAttemptService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttemptService;

    public AuthController(AuthService authService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          LoginAttemptService loginAttemptService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginAttemptService = loginAttemptService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Registration attempt for user: {} email: {}", request.getUsername(), request.getEmailAddress());
        UserAccount user = authService.register(request);
        AuthResponse response = new AuthResponse(user.getUsername(), user.getTenantId(), null);
        logger.info("Registration successful for user: {}", user.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login attempt for principal: {}", request.getUsername());

        if (loginAttemptService.isBlocked(request.getUsername())) {
            logger.warn("Login blocked for principal: {} - Too many failed attempts", request.getUsername());
            return ResponseEntity.status(429).body(ApiResponse.error(429, "Too many failed login attempts. Try again later.", null, java.time.Instant.now().toString()));
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername() + "|" + request.getTenantId(), request.getPassword())
            );
            loginAttemptService.loginSucceeded(request.getUsername());
            UserAccount user = authService.findByUsernameOrEmailAndTenantId(request.getUsername(), request.getTenantId());
            String jwt = jwtTokenProvider.generateToken(user);
            AuthResponse response = new AuthResponse(user.getUsername(), user.getTenantId(), jwt);
            logger.info("Login successful for user: {} tenant: {}", user.getUsername(), user.getTenantId());
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (AuthenticationException ex) {
            loginAttemptService.loginFailed(request.getUsername());
            logger.warn("Login failed for principal: {} - Invalid credentials", request.getUsername());
            return ResponseEntity.status(401).body(ApiResponse.error(401, "Invalid credentials", null, java.time.Instant.now().toString()));
        }
    }
}
