package com.aegispay.settlement;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "settlements", uniqueConstraints = @UniqueConstraint(name = "uk_settlement_payment", columnNames = "payment_id"))
public class Settlement {
    public enum Status { PENDING, SETTLED }

    @Id
    private UUID id;
    @Column(name = "payment_id", nullable = false, updatable = false)
    private String paymentId;
    @Column(name = "merchant_id", nullable = false, updatable = false)
    private String merchantId;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    private String bankReference;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    private Instant settledAt;

    protected Settlement() {}
    public Settlement(String paymentId, String merchantId, BigDecimal amount, String currency) {
        this.id = UUID.randomUUID();
        this.paymentId = paymentId;
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.status = Status.PENDING;
        this.createdAt = Instant.now();
    }
    public UUID getId() { return id; }
    public String getPaymentId() { return paymentId; }
    public String getMerchantId() { return merchantId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Status getStatus() { return status; }
    public String getBankReference() { return bankReference; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getSettledAt() { return settledAt; }
    public void settle() {
        this.status = Status.SETTLED;
        this.bankReference = "BANK-" + UUID.randomUUID();
        this.settledAt = Instant.now();
    }
}
