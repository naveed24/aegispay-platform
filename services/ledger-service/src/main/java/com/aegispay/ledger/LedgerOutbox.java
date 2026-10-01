package com.aegispay.ledger;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_outbox")
public class LedgerOutbox {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String topic;
    @Lob @Column(nullable = false)
    private String payload;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    private Instant publishedAt;

    protected LedgerOutbox() {}
    public LedgerOutbox(UUID id, String topic, String payload) {
        this.id = id; this.topic = topic; this.payload = payload; this.createdAt = Instant.now();
    }
    public UUID getId() { return id; }
    public String getTopic() { return topic; }
    public String getPayload() { return payload; }
    public void markPublished() { this.publishedAt = Instant.now(); }
}
