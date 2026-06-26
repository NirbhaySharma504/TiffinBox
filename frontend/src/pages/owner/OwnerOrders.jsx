import { useEffect, useState } from "react";
import client from "../../api/client";
import StatusBadge from "../../components/StatusBadge";

const STATUSES = ["PLACED", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"];

export default function OwnerOrders() {
  const [orders, setOrders] = useState([]);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);

  function load() {
    Promise.all([
      client.get("/api/orders/owner/today"),
      client.get("/api/orders/owner/summary"),
    ])
      .then(([o, s]) => {
        setOrders(o.data);
        setSummary(s.data);
      })
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  async function changeStatus(id, status) {
    await client.put(`/api/orders/owner/${id}/status`, { status });
    load();
  }

  if (loading) return <p className="text-stone-500">Loading…</p>;

  return (
    <div>
      <h1 className="mb-4 text-2xl font-bold">Today's Orders</h1>

      {summary && (
        <div className="mb-6 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
          <Stat label="Placed" value={summary.placed} />
          <Stat label="Preparing" value={summary.preparing} />
          <Stat label="Out" value={summary.outForDelivery} />
          <Stat label="Delivered" value={summary.delivered} />
          <Stat label="Cancelled" value={summary.cancelled} />
          <Stat label="Revenue" value={`₹${summary.totalRevenue}`} />
        </div>
      )}

      {orders.length === 0 && <p className="text-stone-500">No orders today.</p>}
      <div className="space-y-4">
        {orders.map((order) => (
          <div key={order.id} className="card">
            <div className="mb-2 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="font-semibold">Order #{order.id}</span>
                <StatusBadge status={order.status} />
                <span className="text-sm text-stone-400">user {order.userId}</span>
              </div>
              <select
                className="input w-48"
                value={order.status}
                onChange={(e) => changeStatus(order.id, e.target.value)}
              >
                {STATUSES.map((s) => (
                  <option key={s} value={s}>
                    {s.replaceAll("_", " ")}
                  </option>
                ))}
              </select>
            </div>
            <ul className="text-sm text-stone-600">
              {order.items.map((it) => (
                <li key={it.menuItemId} className="flex justify-between">
                  <span>
                    {it.itemName} × {it.quantity}
                  </span>
                  <span>₹{it.subtotal}</span>
                </li>
              ))}
            </ul>
            <div className="mt-2 border-t border-stone-100 pt-2 text-right text-sm font-semibold">
              Total ₹{order.totalAmount}
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
      <p className="text-xl font-bold">{value}</p>
    </div>
  );
}
