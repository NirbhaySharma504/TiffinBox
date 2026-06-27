import { useEffect, useState } from "react";
import client from "../../api/client";
import StatusBadge from "../../components/StatusBadge";

export default function OwnerFeedback() {
  const [items, setItems] = useState([]);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      client.get("/api/feedback/owner"),
      client.get("/api/feedback/owner/summary"),
    ])
      .then(([a, s]) => {
        setItems(a.data);
        setSummary(s.data);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="text-stone-500">Loading…</p>;

  return (
    <div>
      <h1 className="mb-4 text-2xl font-bold">Customer Feedback</h1>

      {summary && (
        <>
          <div className="mb-4 grid grid-cols-2 gap-3 sm:grid-cols-5">
            <Stat label="Total" value={summary.total} />
            <Stat label="Positive" value={summary.positive} />
            <Stat label="Neutral" value={summary.neutral} />
            <Stat label="Negative" value={summary.negative} />
            <Stat label="Unknown" value={summary.unknown} />
          </div>
          {summary.topThemes?.length > 0 && (
            <div className="card mb-6">
              <p className="mb-2 text-sm font-medium text-stone-600">Top themes (AI-extracted)</p>
              <div className="flex flex-wrap gap-2">
                {summary.topThemes.map((t) => (
                  <span key={t} className="badge bg-brand/10 text-brand">
                    {t}
                  </span>
                ))}
              </div>
            </div>
          )}
        </>
      )}

      {items.length === 0 && <p className="text-stone-500">No feedback yet.</p>}
      <div className="space-y-3">
        {items.map((f) => (
          <div key={f.id} className="card">
            <div className="mb-1 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="font-semibold">Order #{f.orderId}</span>
                {f.rating && <span className="text-amber-500">{"★".repeat(f.rating)}</span>}
                <StatusBadge status={f.sentiment} />
                <span className="text-sm text-stone-400">user {f.userId}</span>
              </div>
            </div>
            <p className="text-sm text-stone-700">{f.comment}</p>
            {f.aiSummary && (
              <p className="mt-1 text-sm italic text-stone-500">“{f.aiSummary}”</p>
            )}
            {f.themes?.length > 0 && (
              <div className="mt-2 flex flex-wrap gap-1">
                {f.themes.map((t) => (
                  <span key={t} className="badge bg-stone-100 text-stone-600">
                    {t}
                  </span>
                ))}
              </div>
            )}
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
