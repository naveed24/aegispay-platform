# API examples

Create a merchant:

```bash
curl -X POST http://localhost:8080/api/merchants \
  -H 'Content-Type: application/json' \
  -d '{"name":"Demo Store","webhookUrl":"https://example.com/webhooks/aegispay"}'
```

Create a payment:

```bash
curl -X POST http://localhost:8080/api/payments \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: 1c17ee94-8bcb-4d03-9b4f-339281779afb' \
  -d '{"merchantId":"<merchant-id>","amount":2500,"currency":"INR"}'
```

Retry the same request with the same idempotency key. AegisPay returns the existing payment rather than creating a second payment.

To exercise the risk engine, create more than five transactions for the same merchant inside one minute or submit a payment of at least 100000.
