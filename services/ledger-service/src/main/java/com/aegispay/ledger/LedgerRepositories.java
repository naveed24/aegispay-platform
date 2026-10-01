package com.aegispay.ledger;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface LedgerTransactionRepository extends JpaRepository<LedgerTransaction, UUID> {
    boolean existsByPaymentId(String paymentId);
}
interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {}
interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {}
interface LedgerOutboxRepository extends JpaRepository<LedgerOutbox, UUID> {
    List<LedgerOutbox> findTop100ByPublishedAtIsNullOrderByCreatedAtAsc();
}
