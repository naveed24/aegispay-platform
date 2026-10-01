package com.aegispay.ledger;

import com.aegispay.events.LedgerPostedEvent;
import com.aegispay.events.PaymentCompletedEvent;
import com.aegispay.events.Topics;
import com.aegispay.ledger.LedgerEntry.Side;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentCompletedListener {
    private final LedgerTransactionRepository transactions;
    private final LedgerEntryRepository entries;
    private final ProcessedEventRepository processed;
    private final LedgerOutboxRepository outbox;
    private final ObjectMapper mapper;

    public PaymentCompletedListener(
            LedgerTransactionRepository transactions,
            LedgerEntryRepository entries,
            ProcessedEventRepository processed,
            LedgerOutboxRepository outbox,
            ObjectMapper mapper) {
        this.transactions = transactions;
        this.entries = entries;
        this.processed = processed;
        this.outbox = outbox;
        this.mapper = mapper;
    }

    @KafkaListener(topics = Topics.PAYMENTS_COMPLETED, groupId = "ledger-service")
    @Transactional
    public void onPaymentCompleted(String payload) throws Exception {
        PaymentCompletedEvent event = mapper.readValue(payload, PaymentCompletedEvent.class);
        if (processed.existsById(event.eventId())) {
            return;
        }

        UUID ledgerId = UUID.randomUUID();
        transactions.save(new LedgerTransaction(
                ledgerId, event.paymentId(), event.merchantId(), event.amount(), event.currency()));

        entries.save(new LedgerEntry(
                ledgerId, "PAYMENT_PROCESSOR_CLEARING", Side.DEBIT, event.amount(), event.currency()));
        entries.save(new LedgerEntry(
                ledgerId, "MERCHANT:" + event.merchantId(), Side.CREDIT, event.amount(), event.currency()));

        processed.save(new ProcessedEvent(event.eventId()));

        LedgerPostedEvent ledgerEvent = new LedgerPostedEvent(
                UUID.randomUUID().toString(),
                ledgerId.toString(),
                event.paymentId(),
                event.merchantId(),
                event.amount(),
                event.currency(),
                Instant.now());

        outbox.save(new LedgerOutbox(
                UUID.fromString(ledgerEvent.eventId()),
                Topics.LEDGER_POSTED,
                mapper.writeValueAsString(ledgerEvent)));
    }
}
