package com.civicpulse.security;

import com.civicpulse.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    @Value("${app.rate-limit.login-max-attempts:5}")
    private int maxAttempts;

    @Value("${app.rate-limit.login-lockout-minutes:15}")
    private int lockoutMinutes;

    private static class Attempt {
        int count;
        Instant lastAttempt;
        Instant lockedUntil;

        Attempt(Instant now) {
            this.count = 1;
            this.lastAttempt = now;
        }
    }

    private final Map<String, Attempt> attemptsCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String key) {
        attemptsCache.remove(key.toLowerCase());
    }

    public void loginFailed(String key) {
        String cleanKey = key.toLowerCase();
        Instant now = Instant.now();
        attemptsCache.compute(cleanKey, (k, attempt) -> {
            if (attempt == null) {
                return new Attempt(now);
            }
            if (attempt.lockedUntil != null && attempt.lockedUntil.isAfter(now)) {
                return attempt; // still locked
            }
            attempt.count++;
            attempt.lastAttempt = now;
            if (attempt.count >= maxAttempts) {
                attempt.lockedUntil = now.plusSeconds((long) lockoutMinutes * 60);
            }
            return attempt;
        });
    }

    public void checkIfBlocked(String key) {
        String cleanKey = key.toLowerCase();
        Attempt attempt = attemptsCache.get(cleanKey);
        if (attempt != null && attempt.lockedUntil != null) {
            Instant now = Instant.now();
            if (attempt.lockedUntil.isAfter(now)) {
                long remainingSeconds = attempt.lockedUntil.getEpochSecond() - now.getEpochSecond();
                throw new RateLimitExceededException("Account temporarily locked due to repeated failed logins. Please try again in " + (remainingSeconds / 60 + 1) + " minutes.", remainingSeconds);
            } else {
                // Lockout expired
                attemptsCache.remove(cleanKey);
            }
        }
    }
}
