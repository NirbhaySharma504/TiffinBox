import { useEffect, useState } from "react";
import client from "../api/client";
import StatusBadge from "../components/StatusBadge";

export default function MySubscriptions() {
  const [subs, setSubs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ mealType: "LUNCH", frequency: "DAILY" });
  const [error, setError] = useState("");

  function load() {
    client.get("/api/subscriptions/me").then((res) => setSubs(res.data));
  }

  useEffect(() => {
    client
      .get("/api/subscriptions/me")
      .then((res) => setSubs(res.data))
      .finally(() => setLoading(false));
  }, []);

  async function create(e) {
    e.preventDefault();
    setError("");
    try {
      await client.post("/api/subscriptions", form);
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not create subscription");
    }
  }

  async function act(id, action) {
    if (action === "cancel") {
      await client.delete(`/api/subscriptions/${id}`);
    } else {
      await client.put(`/api/subscriptions/${id}/${action}`);
    }
    load();
  }

  return (
    <div>
      <h1 className="mb-4 text-2xl font-bold">My Subscriptions</h1>

      <form onSubmit={create} className="card mb-6 flex flex-wrap items-end gap-3">
        <div>
          <label className="label">Meal</label>
          <select
            className="input"
            value={form.mealType}
            onChange={(e) => setForm({ ...form, mealType: e.target.value })}
          >
            <option value="LUNCH">Lunch</option>
            <option value="DINNER">Dinner</option>
          </select>
        </div>
        <div>
          <label className="label">Frequency</label>
          <select
            className="input"
            value={form.frequency}
            onChange={(e) => setForm({ ...form, frequency: e.target.value })}
          >
            <option value="DAILY">Daily</option>
            <option value="WEEKDAYS">Weekdays</option>
          </select>
        </div>
        <button className="btn-primary">Subscribe</button>
        {error && <p className="w-full text-sm text-red-700">{error}</p>}
      </form>

      {loading && <p className="text-stone-500">Loading…</p>}
      {!loading && subs.length === 0 && (
        <p className="text-stone-500">No subscriptions yet.</p>
      )}

      <div className="space-y-3">
        {subs.map((s) => (
          <div key={s.id} className="card flex items-center justify-between">
            <div>
              <div className="flex items-center gap-2">
                <span className="font-semibold">
                  {s.mealType} · {s.frequency}
                </span>
                <StatusBadge status={s.status} />
              </div>
              <p className="text-sm text-stone-500">
                Since {s.startDate}
                {s.lastOrderedOn ? ` · last ordered ${s.lastOrderedOn}` : ""}
              </p>
            </div>
            {s.status !== "CANCELLED" && (
              <div className="flex gap-2">
                {s.status === "ACTIVE" ? (
                  <button className="btn-secondary" onClick={() => act(s.id, "pause")}>
                    Pause
                  </button>
                ) : (
                  <button className="btn-secondary" onClick={() => act(s.id, "resume")}>
                    Resume
                  </button>
                )}
                <button
                  className="btn border border-red-300 text-red-600 hover:bg-red-50"
                  onClick={() => act(s.id, "cancel")}
                >
                  Cancel
                </button>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
