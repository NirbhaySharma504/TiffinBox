import { useState } from "react";
import { useNavigate } from "react-router-dom";
import client from "../api/client";
import { useCart } from "../context/CartContext";

export default function Cart() {
  const { menuId, items, total, setQuantity, clear } = useCart();
  const navigate = useNavigate();
  const [placing, setPlacing] = useState(false);
  const [error, setError] = useState("");

  async function placeOrder() {
    setError("");
    setPlacing(true);
    try {
      await client.post("/api/orders", {
        menuId,
        items: items.map((l) => ({ menuItemId: l.item.id, quantity: l.quantity })),
      });
      clear();
      navigate("/orders");
    } catch (err) {
      setError(err.response?.data?.message || "Could not place order");
    } finally {
      setPlacing(false);
    }
  }

  if (items.length === 0) {
    return (
      <div>
        <h1 className="mb-4 text-2xl font-bold">Your Cart</h1>
        <p className="text-stone-500">Your cart is empty.</p>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-xl">
      <h1 className="mb-4 text-2xl font-bold">Your Cart</h1>
      {error && (
        <p className="mb-3 rounded bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>
      )}
      <div className="card space-y-3">
        {items.map((l) => (
          <div key={l.item.id} className="flex items-center justify-between">
            <div>
              <p className="font-medium">{l.item.name}</p>
              <p className="text-sm text-stone-500">₹{l.item.price} each</p>
            </div>
            <div className="flex items-center gap-3">
              <input
                type="number"
                min="0"
                className="input w-20"
                value={l.quantity}
                onChange={(e) => setQuantity(l.item.id, parseInt(e.target.value || "0", 10))}
              />
              <span className="w-20 text-right font-semibold">
                ₹{(Number(l.item.price) * l.quantity).toFixed(2)}
              </span>
            </div>
          </div>
        ))}
        <div className="flex items-center justify-between border-t border-stone-200 pt-3">
          <span className="text-lg font-semibold">Total</span>
          <span className="text-lg font-bold text-brand">₹{total.toFixed(2)}</span>
        </div>
        <button className="btn-primary w-full" onClick={placeOrder} disabled={placing}>
          {placing ? "Placing order…" : "Place order"}
        </button>
      </div>
    </div>
  );
}
