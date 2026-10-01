package com.aegispay.merchant;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "merchants")
public class Merchant {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String apiKey;
    private String webhookUrl;
    @Column(nullable = false)
    private boolean active;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Merchant() {}

    public Merchant(UUID id, String name, String apiKey, String webhookUrl) {
        this.id = id;
        this.name = name;
        this.apiKey = apiKey;
        this.webhookUrl = webhookUrl;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getApiKey() { return apiKey; }
    public String getWebhookUrl() { return webhookUrl; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
    public void setActive(boolean active) { this.active = active; }
}
