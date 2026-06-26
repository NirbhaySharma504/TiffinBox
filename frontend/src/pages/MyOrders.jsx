import { useEffect, useState } from "react";
import client from "../api/client";
import StatusBadge from "../components/StatusBadge";

export default function MyOrders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    client
      .get("/api/orders/me")
      .then((res) => setOrders(res.data))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="text-stone-500">Loading orders…</p>;

  return (
    <div>
      <h1 className="mb-4 text-2xl font-bold">My Orders</h1>
      {orders.length === 0 && <p className="text-stone-500">No orders yet.</p>}
      <div className="space-y-4">
        {orders.map((order) => (
          <div key={order.id} className="card">
            <div className="mb-2 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="font-semibold">Order #{order.id}</span>
                <StatusBadge status={order.status} />
              </div>
              <span className="text-sm text-stone-400">
                {new Date(order.createdAt).toLocaleString()}
              </span>
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
            <div className="mt-2 flex justify-between border-t border-stone-100 pt-2 text-sm">
              <span className="text-stone-500">
                {order.paymentId ? `Payment #${order.paymentId}` : "Payment pending"}
              </span>
              <span className="font-semibold">Total ₹{order.totalAmount}</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
