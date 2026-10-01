import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '20s', target: 20 },
    { duration: '40s', target: 100 },
    { duration: '20s', target: 0 }
  ],
  thresholds: {
    http_req_failed: ['rate<0.02'],
    http_req_duration: ['p(95)<500']
  }
};

const merchantId = __ENV.MERCHANT_ID;
const apiKey = __ENV.API_KEY || '';

export default function () {
  const payload = JSON.stringify({
    merchantId,
    amount: Math.floor(Math.random() * 50000) + 100,
    currency: 'INR'
  });

  const response = http.post('http://localhost:8080/api/payments', payload, {
    headers: {
      'Content-Type': 'application/json',
      'Idempotency-Key': crypto.randomUUID ? crypto.randomUUID() : String(Math.random()),
      ...(apiKey ? { 'X-API-Key': apiKey } : {})
    }
  });

  check(response, {
    'payment accepted': r => [200, 201].includes(r.status)
  });
  sleep(0.2);
}
