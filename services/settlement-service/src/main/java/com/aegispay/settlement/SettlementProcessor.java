package com.aegispay.settlement;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SettlementProcessor {
    private final SettlementRepository repository;

    public SettlementProcessor(SettlementRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedDelayString = "${settlement.processor-delay-ms:5000}")
    @Transactional
    public void settlePending() {
        for (Settlement settlement : repository.findTop100ByStatusOrderByCreatedAtAsc(Settlement.Status.PENDING)) {
            settlement.settle();
        }
    }
}
