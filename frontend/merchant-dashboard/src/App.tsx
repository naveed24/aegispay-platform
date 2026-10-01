import { FormEvent, useEffect, useMemo, useState } from 'react';

type Payment = {
  id: string;
  merchantId: string;
  amount: number;
  currency: string;
  status: 'PENDING' | 'SUCCEEDED' | 'REJECTED' | 'FAILED' | 'UNKNOWN';
  processorReference?: string;
  failureReason?: string;
  createdAt: string;
};

type Merchant = {
  id: string;
  name: string;
  webhookUrl?: string;
  active: boolean;
};

const money = new Intl.NumberFormat('en-IN', { maximumFractionDigits: 2 });

export default function App() {
  const [payments, setPayments] = useState<Payment[]>([]);
  const [merchants, setMerchants] = useState<Merchant[]>([]);
  const [merchantId, setMerchantId] = useState('');
  const [amount, setAmount] = useState('1000');
  const [currency, setCurrency] = useState('INR');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  async function load() {
    const [paymentRes, merchantRes] = await Promise.all([
      fetch('/api/payments'),
      fetch('/api/merchants')
    ]);
    if (paymentRes.ok) setPayments(await paymentRes.json());
    if (merchantRes.ok) {
      const data: Merchant[] = await merchantRes.json();
      setMerchants(data);
      setMerchantId(current => current || data[0]?.id || '');
    }
  }

  useEffect(() => {
    load().catch(() => setError('Could not reach AegisPay services.'));
    const timer = setInterval(() => load().catch(() => undefined), 5000);
    return () => clearInterval(timer);
  }, []);

  const metrics = useMemo(() => {
    const success = payments.filter(p => p.status === 'SUCCEEDED').length;
    const rejected = payments.filter(p => p.status === 'REJECTED').length;
    const volume = payments.filter(p => p.status === 'SUCCEEDED')
      .reduce((sum, p) => sum + Number(p.amount), 0);
    return {
      total: payments.length,
      successRate: payments.length ? (success / payments.length) * 100 : 0,
      rejected,
      volume
    };
  }, [payments]);

  async function submitPayment(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError('');
    try {
      const response = await fetch('/api/payments', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Idempotency-Key': crypto.randomUUID()
        },
        body: JSON.stringify({ merchantId, amount: Number(amount), currency })
      });
      if (!response.ok) throw new Error(await response.text());
      await load();
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Payment failed');
    } finally {
      setBusy(false);
    }
  }

  return (
    <main>
      <header>
        <div>
          <span className="eyebrow">FINANCIAL INFRASTRUCTURE</span>
          <h1>AegisPay Control Plane</h1>
          <p>Payments, risk decisions, ledger propagation and merchant operations.</p>
        </div>
        <div className="live"><span /> LIVE</div>
      </header>

      <section className="metrics">
        <Metric label="Processed" value={String(metrics.total)} />
        <Metric label="Success rate" value={metrics.successRate.toFixed(1) + '%'} />
        <Metric label="Blocked by risk" value={String(metrics.rejected)} />
        <Metric label="Succeeded volume" value={'₹' + money.format(metrics.volume)} />
      </section>

      <section className="grid">
        <div className="panel">
          <div className="panelTitle"><h2>Simulate payment</h2><span>Idempotent API</span></div>
          <form onSubmit={submitPayment}>
            <label>Merchant
              <select value={merchantId} onChange={e => setMerchantId(e.target.value)} required>
                <option value="">Create a merchant through API first</option>
                {merchants.map(m => <option key={m.id} value={m.id}>{m.name}</option>)}
              </select>
            </label>
            <div className="row">
              <label>Amount<input value={amount} onChange={e => setAmount(e.target.value)} type="number" min="0.01" step="0.01" /></label>
              <label>Currency
                <select value={currency} onChange={e => setCurrency(e.target.value)}>
                  <option>INR</option><option>USD</option><option>EUR</option><option>GBP</option>
                </select>
              </label>
            </div>
            <button disabled={busy || !merchantId}>{busy ? 'Processing…' : 'Process payment'}</button>
            {error && <p className="error">{error}</p>}
          </form>
        </div>

        <div className="panel">
          <div className="panelTitle"><h2>Architecture signal</h2><span>Event driven</span></div>
          <div className="flow">
            <Flow name="Gateway" state="healthy" />
            <Flow name="Payment" state="idempotent" />
            <Flow name="Fraud" state="Redis risk" />
            <Flow name="Kafka" state="outbox" />
            <Flow name="Ledger" state="double-entry" />
            <Flow name="Settlement" state="async" />
          </div>
        </div>
      </section>

      <section className="panel tablePanel">
        <div className="panelTitle"><h2>Recent payments</h2><button className="ghost" onClick={() => load()}>Refresh</button></div>
        <div className="tableWrap">
          <table>
            <thead><tr><th>Payment</th><th>Merchant</th><th>Amount</th><th>Status</th><th>Processor ref</th></tr></thead>
            <tbody>
              {[...payments].reverse().slice(0, 20).map(p => (
                <tr key={p.id}>
                  <td className="mono">{p.id.slice(0, 8)}…</td>
                  <td className="mono">{p.merchantId.slice(0, 8)}…</td>
                  <td>{p.currency} {money.format(p.amount)}</td>
                  <td><span className={'status ' + p.status.toLowerCase()}>{p.status}</span></td>
                  <td className="mono">{p.processorReference || p.failureReason || '—'}</td>
                </tr>
              ))}
              {!payments.length && <tr><td colSpan={5} className="empty">No payments yet.</td></tr>}
            </tbody>
          </table>
        </div>
      </section>
    </main>
  );
}

function Metric({ label, value }: { label: string; value: string }) {
  return <div className="metric"><span>{label}</span><strong>{value}</strong></div>;
}

function Flow({ name, state }: { name: string; state: string }) {
  return <div className="flowItem"><div><b>{name}</b><small>{state}</small></div><span>→</span></div>;
}
