package com.aegispay.settlement;

import com.aegispay.events.LedgerPostedEvent;
import com.aegispay.events.Topics;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LedgerPostedListener {
    private final SettlementRepository repository;
    private final ObjectMapper mapper;

    public LedgerPostedListener(SettlementRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @KafkaListener(topics = Topics.LEDGER_POSTED, groupId = "settlement-service")
    @Transactional
    public void onLedgerPosted(String payload) throws Exception {
        LedgerPostedEvent event = mapper.readValue(payload, LedgerPostedEvent.class);
        if (repository.existsByPaymentId(event.paymentId())) {
            return;
        }
        repository.save(new Settlement(event.paymentId(), event.merchantId(), event.amount(), event.currency()));
    }
}
