# AegisPay Architecture

AegisPay is a domain-oriented microservices platform for payment processing.

## Core runtime path

1. Client submits a payment with an `Idempotency-Key`.
2. Payment Service persists a PENDING payment and outbox event in one local transaction.
3. Fraud Service scores the transaction using deterministic rules and Redis velocity counters.
4. Approved payments transition to SUCCEEDED.
5. Transactional outbox publishes `payments.completed` to Kafka.
6. Ledger Service consumes the event idempotently and writes immutable double-entry ledger entries.
7. Ledger Service emits `ledger.posted`.
8. Settlement Service creates a merchant settlement record.
9. Webhook Service creates and retries merchant webhook deliveries.
10. Every service exposes Actuator metrics for Prometheus.

## Consistency model

The platform does not use distributed XA transactions. It uses:
- database-per-service ownership
- transactional outbox
- at-least-once Kafka delivery
- idempotent consumers
- immutable financial ledger entries
- eventual consistency
- retry with backoff
- reconciliation-friendly states

## Data ownership

| Service | Primary store |
|---|---|
| Merchant | PostgreSQL merchant_db |
| Payment | PostgreSQL payment_db |
| Fraud | Redis + stateless rule engine |
| Ledger | PostgreSQL ledger_db |
| Settlement | PostgreSQL settlement_db |
| Webhook | PostgreSQL webhook_db |

## Reliability

Every event consumer stores a processed event identifier before applying a second time. Financial state changes use explicit state transitions rather than silent overwrites.

See `docs/adr` for the major architectural decisions.
