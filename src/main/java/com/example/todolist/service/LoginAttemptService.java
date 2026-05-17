package com.example.todolist.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    private final ConcurrentMap<String, Attempt> attemptCache = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        Attempt attempt = attemptCache.get(username);
        if (attempt == null) {
            return false;
        }

        if (attempt.lockExpires != null && attempt.lockExpires.isAfter(Instant.now())) {
            return true;
        }

        if (attempt.lockExpires != null && attempt.lockExpires.isBefore(Instant.now())) {
            attemptCache.remove(username);
            return false;
        }

        return false;
    }

    public void loginSucceeded(String username) {
        attemptCache.remove(username);
    }

    public void loginFailed(String username) {
        attemptCache.compute(username, (key, attempt) -> {
            if (attempt == null || attempt.lockExpires != null && attempt.lockExpires.isBefore(Instant.now())) {
                return new Attempt(1, null);
            }

            int nextAttempts = attempt.attempts + 1;
            if (nextAttempts >= MAX_ATTEMPTS) {
                return new Attempt(nextAttempts, Instant.now().plus(BLOCK_DURATION));
            }
            return new Attempt(nextAttempts, null);
        });
    }

    private static class Attempt {
        final int attempts;
        final Instant lockExpires;

        Attempt(int attempts, Instant lockExpires) {
            this.attempts = attempts;
            this.lockExpires = lockExpires;
        }
    }
}
