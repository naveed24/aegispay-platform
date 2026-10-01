package com.aegispay.webhook;

import com.aegispay.events.PaymentCompletedEvent;
import com.aegispay.events.Topics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentWebhookListener {
    private final WebhookDeliveryRepository repository;
    private final ObjectMapper mapper;

    public PaymentWebhookListener(WebhookDeliveryRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @KafkaListener(topics = Topics.PAYMENTS_COMPLETED, groupId = "webhook-service")
    @Transactional
    public void onPaymentCompleted(String payload) throws Exception {
        PaymentCompletedEvent event = mapper.readValue(payload, PaymentCompletedEvent.class);
        if (!repository.existsByEventId(event.eventId())) {
            repository.save(new WebhookDelivery(event.eventId(), event.merchantId(), payload));
        }
    }
}
