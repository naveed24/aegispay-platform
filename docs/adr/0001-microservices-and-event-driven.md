# ADR 0001: Domain microservices with Kafka

**Status:** Accepted

AegisPay separates payment orchestration, risk, ledger, settlement, merchant configuration and webhook delivery because these domains have different correctness, scaling and failure characteristics.

Synchronous HTTP is used where an immediate decision is required, such as risk scoring. Kafka is used for durable propagation of completed business facts.

We explicitly avoid a shared database and distributed transactions. Cross-service consistency is achieved through outbox publishing, idempotent consumers and compensating/reconciliation processes.
