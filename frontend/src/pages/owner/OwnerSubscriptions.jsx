import { useEffect, useState } from "react";
import client from "../../api/client";
import StatusBadge from "../../components/StatusBadge";

export default function OwnerSubscriptions() {
  const [subs, setSubs] = useState([]);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");

  function load() {
    Promise.all([
      client.get("/api/subscriptions/owner"),
      client.get("/api/subscriptions/owner/summary"),
    ]).then(([a, s]) => {
      setSubs(a.data);
      setSummary(s.data);
    });
  }

  useEffect(() => {
    Promise.all([
      client.get("/api/subscriptions/owner"),
      client.get("/api/subscriptions/owner/summary"),
    ])
      .then(([a, s]) => {
        setSubs(a.data);
        setSummary(s.data);
      })
      .finally(() => setLoading(false));
  }, []);

  async function runDue() {
    setMessage("");
    const { data } = await client.post("/api/subscriptions/owner/run-due");
    setMessage(`Auto-order run complete: ${data.ordersPlaced} order(s) placed.`);
    load();
  }

  if (loading) return <p className="text-stone-500">Loading…</p>;

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-2xl font-bold">Subscriptions</h1>
        <button className="btn-primary" onClick={runDue}>
          Run auto-orders now
        </button>
      </div>

      {message && (
        <p className="mb-4 rounded bg-green-50 px-3 py-2 text-sm text-green-700">
          {message}
        </p>
      )}

      {summary && (
        <div className="mb-6 grid grid-cols-3 gap-3">
          <Stat label="Active" value={summary.active} />
          <Stat label="Paused" value={summary.paused} />
          <Stat label="Cancelled" value={summary.cancelled} />
        </div>
      )}

      <div className="space-y-3">
        {subs.map((s) => (
          <div key={s.id} className="card flex items-center justify-between">
            <div>
              <div className="flex items-center gap-2">
                <span className="font-semibold">
                  #{s.id} · {s.mealType} · {s.frequency}
                </span>
                <StatusBadge status={s.status} />
              </div>
              <p className="text-sm text-stone-500">
                user {s.userId} · {s.customerEmail}
                {s.lastOrderedOn ? ` · last ordered ${s.lastOrderedOn}` : ""}
              </p>
            </div>
          </div>
        ))}
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
