package com.aegispay.payment;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String topic;
    @Lob
    @Column(nullable = false)
    private String payload;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    private Instant publishedAt;

    protected OutboxEvent() {}

    public OutboxEvent(UUID id, String topic, String payload) {
        this.id = id;
        this.topic = topic;
        this.payload = payload;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getTopic() { return topic; }
    public String getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public void markPublished() { this.publishedAt = Instant.now(); }
}
