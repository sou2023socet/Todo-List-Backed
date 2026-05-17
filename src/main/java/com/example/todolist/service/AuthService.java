package com.example.todolist.service;

import com.example.todolist.dto.RegisterRequest;
import com.example.todolist.model.UserAccount;
import com.example.todolist.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAccount register(RegisterRequest authRequest) {
        if (authRequest.getEmailAddress() == null || authRequest.getEmailAddress().isBlank()) {
            throw new IllegalArgumentException("Email address is required for registration");
        }

        if (userRepository.existsByUsernameOrEmailAddress(authRequest.getUsername(), authRequest.getEmailAddress())) {
            throw new IllegalArgumentException("Username or email already exists");
        }

        UserAccount user = new UserAccount(
                authRequest.getUsername(),
                passwordEncoder.encode(authRequest.getPassword()),
                authRequest.getTenantId(),
                authRequest.getEmailAddress()
        );

        return userRepository.save(user);
    }

    public UserAccount findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsernameOrEmailAddress(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public UserAccount findByUsernameOrEmailAndTenantId(String usernameOrEmail, String tenantId) {
        if (usernameOrEmail.contains("@")) {
            return userRepository.findByEmailAddressAndTenantId(usernameOrEmail, tenantId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found for tenant"));
        }
        return userRepository.findByUsernameAndTenantId(usernameOrEmail, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("User not found for tenant"));
    }

    public UserAccount findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
