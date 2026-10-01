package com.aegispay.webhook;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Component
public class WebhookProcessor {
    private final WebhookDeliveryRepository repository;
    private final RestClient merchantClient;
    private final RestClient http = RestClient.create();
    private final String signingSecret;

    public WebhookProcessor(
            WebhookDeliveryRepository repository,
            RestClient merchantClient,
            @Value("${webhook.signing-secret:change-me-in-production}") String signingSecret) {
        this.repository = repository;
        this.merchantClient = merchantClient;
        this.signingSecret = signingSecret;
    }

    @Scheduled(fixedDelayString = "${webhook.worker-delay-ms:2000}")
    @Transactional
    public void process() {
        var due = repository.findTop50ByStatusInAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
                List.of(WebhookDelivery.Status.PENDING, WebhookDelivery.Status.RETRYING), Instant.now());

        for (WebhookDelivery delivery : due) {
            try {
                WebhookConfig config = merchantClient.get()
                        .uri("/api/merchants/{id}/webhook", delivery.getMerchantId())
                        .retrieve()
                        .body(WebhookConfig.class);

                if (config == null || !config.active() || config.webhookUrl() == null || config.webhookUrl().isBlank()) {
                    delivery.dead("Merchant webhook is not configured");
                    continue;
                }

                http.post()
                        .uri(config.webhookUrl())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-AegisPay-Event-Id", delivery.getEventId())
                        .header("X-AegisPay-Signature", sign(delivery.getPayload()))
                        .body(delivery.getPayload())
                        .retrieve()
                        .toBodilessEntity();

                delivery.delivered();
            } catch (Exception e) {
                delivery.retry(e.getMessage());
            }
        }
    }

    private String sign(String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "sha256=" + HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }

    public record WebhookConfig(String merchantId, String webhookUrl, boolean active) {}
}
