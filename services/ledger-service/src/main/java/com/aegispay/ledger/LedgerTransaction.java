package com.aegispay.ledger;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_transactions", uniqueConstraints = @UniqueConstraint(name = "uk_ledger_payment", columnNames = "payment_id"))
public class LedgerTransaction {
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
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected LedgerTransaction() {}

    public LedgerTransaction(UUID id, String paymentId, String merchantId, BigDecimal amount, String currency) {
        this.id = id;
        this.paymentId = paymentId;
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getPaymentId() { return paymentId; }
    public String getMerchantId() { return merchantId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }
}
