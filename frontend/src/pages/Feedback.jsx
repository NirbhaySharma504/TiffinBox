import { useEffect, useState } from "react";
import client from "../api/client";
import StatusBadge from "../components/StatusBadge";

export default function Feedback() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ orderId: "", rating: "5", comment: "" });
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  function load() {
    client.get("/api/feedback/me").then((res) => setItems(res.data));
  }

  useEffect(() => {
    client
      .get("/api/feedback/me")
      .then((res) => setItems(res.data))
      .finally(() => setLoading(false));
  }, []);

  async function submit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await client.post("/api/feedback", {
        orderId: Number(form.orderId),
        rating: Number(form.rating),
        comment: form.comment,
      });
      setForm({ orderId: "", rating: "5", comment: "" });
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not submit feedback");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <h1 className="mb-4 text-2xl font-bold">Feedback</h1>

      <form onSubmit={submit} className="card mb-6 space-y-3">
        <h2 className="font-semibold">Leave feedback on an order</h2>
        {error && (
          <p className="rounded bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>
        )}
        <div className="flex flex-wrap gap-3">
          <div>
            <label className="label">Order #</label>
            <input
              type="number"
              className="input w-28"
              value={form.orderId}
              onChange={(e) => setForm({ ...form, orderId: e.target.value })}
              required
            />
          </div>
          <div>
            <label className="label">Rating</label>
            <select
              className="input w-24"
              value={form.rating}
              onChange={(e) => setForm({ ...form, rating: e.target.value })}
            >
              {[5, 4, 3, 2, 1].map((n) => (
                <option key={n} value={n}>
                  {n} ★
                </option>
              ))}
            </select>
          </div>
        </div>
        <div>
          <label className="label">Comment</label>
          <textarea
            className="input"
            rows={3}
            value={form.comment}
            onChange={(e) => setForm({ ...form, comment: e.target.value })}
            placeholder="How was your meal?"
            required
          />
        </div>
        <button className="btn-primary" disabled={submitting}>
          {submitting ? "Analyzing…" : "Submit feedback"}
        </button>
      </form>

      {loading && <p className="text-stone-500">Loading…</p>}
      {!loading && items.length === 0 && (
        <p className="text-stone-500">No feedback yet.</p>
      )}
      <div className="space-y-3">
        {items.map((f) => (
          <div key={f.id} className="card">
            <div className="mb-1 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="font-semibold">Order #{f.orderId}</span>
                {f.rating && <span className="text-amber-500">{"★".repeat(f.rating)}</span>}
                <StatusBadge status={f.sentiment} />
              </div>
              <span className="text-xs text-stone-400">
                {f.analyzedByAi ? "AI-analyzed" : "auto"}
              </span>
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
