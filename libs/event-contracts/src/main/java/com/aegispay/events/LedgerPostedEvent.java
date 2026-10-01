package com.aegispay.events;

import java.math.BigDecimal;
import java.time.Instant;

public record LedgerPostedEvent(
        String eventId,
        String ledgerTransactionId,
        String paymentId,
        String merchantId,
        BigDecimal amount,
        String currency,
        Instant occurredAt
) {}
