package com.aegispay.webhook;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "webhook_deliveries", uniqueConstraints = @UniqueConstraint(name = "uk_webhook_event", columnNames = "event_id"))
public class WebhookDelivery {
    public enum Status { PENDING, RETRYING, DELIVERED, DEAD }

    @Id
    private UUID id;
    @Column(name = "event_id", nullable = false, updatable = false)
    private String eventId;
    @Column(name = "merchant_id", nullable = false, updatable = false)
    private String merchantId;
    @Lob @Column(nullable = false, updatable = false)
    private String payload;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    @Column(nullable = false)
    private int attempts;
    @Column(nullable = false)
    private Instant nextAttemptAt;
    private Instant deliveredAt;
    @Column(length = 1000)
    private String lastError;

    protected WebhookDelivery() {}

    public WebhookDelivery(String eventId, String merchantId, String payload) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.merchantId = merchantId;
        this.payload = payload;
        this.status = Status.PENDING;
        this.nextAttemptAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getEventId() { return eventId; }
    public String getMerchantId() { return merchantId; }
    public String getPayload() { return payload; }
    public Status getStatus() { return status; }
    public int getAttempts() { return attempts; }
    public Instant getNextAttemptAt() { return nextAttemptAt; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public String getLastError() { return lastError; }

    public void delivered() {
        this.status = Status.DELIVERED;
        this.deliveredAt = Instant.now();
        this.lastError = null;
    }

    public void retry(String error) {
        attempts++;
        lastError = error == null ? "delivery failed" : error.substring(0, Math.min(error.length(), 1000));
        if (attempts >= 6) {
            status = Status.DEAD;
            return;
        }
        status = Status.RETRYING;
        long delaySeconds = Math.min(3600, 5L * (1L << Math.min(attempts, 10)));
        nextAttemptAt = Instant.now().plusSeconds(delaySeconds);
    }

    public void dead(String reason) {
        status = Status.DEAD;
        lastError = reason;
    }
}
