package com.aegispay.fraud;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RiskEngine {
    private static final BigDecimal HIGH_VALUE = new BigDecimal("100000");
    private final StringRedisTemplate redis;

    public RiskEngine(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public RiskResult score(RiskRequest request) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        if (request.amount().compareTo(HIGH_VALUE) >= 0) {
            score += 35;
            reasons.add("HIGH_VALUE_PAYMENT");
        }

        String velocityKey = "risk:merchant:" + request.merchantId() + ":payments:1m";
        Long velocity = redis.opsForValue().increment(velocityKey);
        if (velocity != null && velocity == 1L) {
            redis.expire(velocityKey, Duration.ofMinutes(1));
        }
        if (velocity != null && velocity > 5) {
            score += 50;
            reasons.add("HIGH_TRANSACTION_VELOCITY");
        }

        if (!List.of("INR", "USD", "EUR", "GBP").contains(request.currency().toUpperCase())) {
            score += 25;
            reasons.add("UNUSUAL_CURRENCY");
        }

        boolean approved = score < 70;
        return new RiskResult(approved, score, reasons);
    }

    public record RiskRequest(String merchantId, String paymentId, BigDecimal amount, String currency) {}
    public record RiskResult(boolean approved, int score, List<String> reasons) {}
}
