package com.aegispay.payment;

import com.aegispay.events.PaymentCompletedEvent;
import com.aegispay.events.Topics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentFinalizer {
    private final PaymentRepository payments;
    private final OutboxRepository outbox;
    private final ObjectMapper objectMapper;

    public PaymentFinalizer(PaymentRepository payments, OutboxRepository outbox, ObjectMapper objectMapper) {
        this.payments = payments;
        this.outbox = outbox;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Payment complete(UUID paymentId, RiskResponse risk) {
        Payment payment = payments.findById(paymentId).orElseThrow();
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return payment;
        }

        if (!risk.approved()) {
            payment.reject(String.join(",", risk.reasons()));
            return payment;
        }

        payment.succeed("SIM-" + UUID.randomUUID());
        PaymentCompletedEvent event = new PaymentCompletedEvent(
                UUID.randomUUID().toString(),
                payment.getId().toString(),
                payment.getMerchantId(),
                payment.getAmount(),
                payment.getCurrency(),
                Instant.now());

        try {
            outbox.save(new OutboxEvent(UUID.fromString(event.eventId()), Topics.PAYMENTS_COMPLETED, objectMapper.writeValueAsString(event)));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize payment event", e);
        }
        return payment;
    }

    public record RiskResponse(boolean approved, int score, java.util.List<String> reasons) {}
}
