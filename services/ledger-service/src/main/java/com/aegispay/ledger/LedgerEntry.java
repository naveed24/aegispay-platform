package com.aegispay.ledger;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {
    public enum Side { DEBIT, CREDIT }

    @Id
    private UUID id;
    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;
    @Column(name = "account_code", nullable = false, updatable = false)
    private String accountCode;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private Side side;
    @Column(nullable = false, precision = 19, scale = 4, updatable = false)
    private BigDecimal amount;
    @Column(nullable = false, length = 3, updatable = false)
    private String currency;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected LedgerEntry() {}

    public LedgerEntry(UUID transactionId, String accountCode, Side side, BigDecimal amount, String currency) {
        this.id = UUID.randomUUID();
        this.transactionId = transactionId;
        this.accountCode = accountCode;
        this.side = side;
        this.amount = amount;
        this.currency = currency;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getTransactionId() { return transactionId; }
    public String getAccountCode() { return accountCode; }
    public Side getSide() { return side; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
}
