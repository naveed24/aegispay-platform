package com.aegispay.ledger;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "processed_events")
public class ProcessedEvent {
    @Id
    private String id;
    @Column(nullable = false, updatable = false)
    private Instant processedAt;

    protected ProcessedEvent() {}
    public ProcessedEvent(String id) {
        this.id = id;
        this.processedAt = Instant.now();
    }
}
