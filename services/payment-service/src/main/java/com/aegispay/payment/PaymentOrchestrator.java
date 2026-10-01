package com.aegispay.payment;

import com.aegispay.payment.PaymentFinalizer.RiskResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PaymentOrchestrator {
    private final PaymentRepository payments;
    private final PaymentFinalizer finalizer;
    private final RestClient fraudClient;
    private final RestClient merchantClient;

    public PaymentOrchestrator(
            PaymentRepository payments,
            PaymentFinalizer finalizer,
            RestClient fraudClient,
            RestClient merchantClient) {
        this.payments = payments;
        this.finalizer = finalizer;
        this.fraudClient = fraudClient;
        this.merchantClient = merchantClient;
    }

    public Payment create(CreatePaymentCommand command, String idempotencyKey, String apiKey) {
        merchantClient.post()
                .uri(uri -> uri.path("/internal/merchants/validate")
                        .queryParam("merchantId", command.merchantId())
                        .build())
                .header("X-API-Key", apiKey)
                .retrieve()
                .toBodilessEntity();

        String requestHash = RequestHasher.hash(command.merchantId(), command.amount(), command.currency());

        var existing = payments.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            verifySameRequest(existing.get(), requestHash);
            return existing.get();
        }

        Payment payment = new Payment(
                UUID.randomUUID(),
                command.merchantId(),
                command.amount(),
                command.currency().toUpperCase(),
                idempotencyKey,
                requestHash);

        try {
            payment = payments.saveAndFlush(payment);
        } catch (DataIntegrityViolationException race) {
            Payment winner = payments.findByIdempotencyKey(idempotencyKey).orElseThrow(() -> race);
            verifySameRequest(winner, requestHash);
            return winner;
        }

        RiskResponse risk = fraudClient.post()
                .uri("/internal/risk/score")
                .body(new RiskRequest(payment.getMerchantId(), payment.getId().toString(), payment.getAmount(), payment.getCurrency()))
                .retrieve()
                .body(RiskResponse.class);

        if (risk == null) {
            risk = new RiskResponse(false, 100, List.of("RISK_ENGINE_EMPTY_RESPONSE"));
        }
        return finalizer.complete(payment.getId(), risk);
    }

    private static void verifySameRequest(Payment payment, String requestHash) {
        if (!payment.getRequestHash().equals(requestHash)) {
            throw new IdempotencyConflictException();
        }
    }

    public record CreatePaymentCommand(String merchantId, BigDecimal amount, String currency) {}
    public record RiskRequest(String merchantId, String paymentId, BigDecimal amount, String currency) {}

    public static class IdempotencyConflictException extends RuntimeException {}
}
