package com.aegispay.settlement;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, UUID> {
    boolean existsByPaymentId(String paymentId);
    List<Settlement> findTop100ByStatusOrderByCreatedAtAsc(Settlement.Status status);
}
