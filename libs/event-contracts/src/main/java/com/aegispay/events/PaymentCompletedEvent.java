package com.aegispay.events;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCompletedEvent(
        String eventId,
        String paymentId,
        String merchantId,
        BigDecimal amount,
        String currency,
        Instant occurredAt
) {}
