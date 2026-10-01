package com.aegispay.payment;

import java.util.concurrent.TimeUnit;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxPublisher {
    private final OutboxRepository repository;
    private final KafkaTemplate<String, String> kafka;

    public OutboxPublisher(OutboxRepository repository, KafkaTemplate<String, String> kafka) {
        this.repository = repository;
        this.kafka = kafka;
    }

    @Scheduled(fixedDelayString = "${outbox.publish-delay-ms:1000}")
    @Transactional
    public void publish() {
        for (OutboxEvent event : repository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            try {
                kafka.send(event.getTopic(), event.getId().toString(), event.getPayload()).get(5, TimeUnit.SECONDS);
                event.markPublished();
            } catch (Exception e) {
                break;
            }
        }
    }
}
