package com.aegispay.payment;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments", uniqueConstraints = @UniqueConstraint(name = "uk_payment_idempotency", columnNames = "idempotency_key"))
public class Payment {
    @Id
    private UUID id;
    @Column(name = "merchant_id", nullable = false)
    private String merchantId;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    @Column(name = "idempotency_key", nullable = false, updatable = false)
    private String idempotencyKey;
    @Column(name = "request_hash", nullable = false, updatable = false, length = 64)
    private String requestHash;
    @Column(name = "processor_reference")
    private String processorReference;
    @Column(name = "failure_reason")
    private String failureReason;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    protected Payment() {}

    public Payment(UUID id, String merchantId, BigDecimal amount, String currency, String idempotencyKey, String requestHash) {
        this.id = id;
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.status = PaymentStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() { return id; }
    public String getMerchantId() { return merchantId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getRequestHash() { return requestHash; }
    public String getProcessorReference() { return processorReference; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void succeed(String processorReference) {
        this.status = PaymentStatus.SUCCEEDED;
        this.processorReference = processorReference;
        this.failureReason = null;
        this.updatedAt = Instant.now();
    }

    public void reject(String reason) {
        this.status = PaymentStatus.REJECTED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }
}
