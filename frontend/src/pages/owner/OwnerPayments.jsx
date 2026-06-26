import { useEffect, useState } from "react";
import client from "../../api/client";

export default function OwnerPayments() {
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    client
      .get("/api/payments/owner/summary")
      .then((res) => setSummary(res.data))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="text-stone-500">Loading…</p>;

  return (
    <div>
      <h1 className="mb-4 text-2xl font-bold">Payments</h1>
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <Stat label="Collected" value={`₹${summary.totalCollected}`} />
        <Stat label="Paid" value={summary.paidCount} />
        <Stat label="Pending" value={summary.pendingCount} />
        <Stat label="Failed" value={summary.failedCount} />
      </div>
    </div>
  );
}

function Stat({ label, value }) {
  return (
    <div className="card text-center">
      <p className="text-xs uppercase text-stone-400">{label}</p>
      <p className="text-2xl font-bold">{value}</p>
    </div>
  );
}
