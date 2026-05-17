package com.example.todolist.service;

import com.example.todolist.model.UserAccount;
import com.example.todolist.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount account = userRepository.findByUsernameOrEmailAddress(username, username)
                .orElseThrow(() -> {
                    logger.warn("User not found for login attempt: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        logger.debug("Loaded user for authentication: {}", account.getUsername());
        return User.withUsername(account.getUsername())
                .password(account.getPassword())
                .roles("USER")
                .build();
    }
}
