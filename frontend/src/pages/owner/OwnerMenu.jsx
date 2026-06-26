import { useEffect, useState } from "react";
import client from "../../api/client";
import StatusBadge from "../../components/StatusBadge";

const emptyItem = () => ({ name: "", price: "", description: "" });

export default function OwnerMenu() {
  const [menus, setMenus] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [form, setForm] = useState({
    date: new Date().toISOString().slice(0, 10),
    mealType: "LUNCH",
    description: "",
    cutoffTime: "11:00",
    items: [emptyItem()],
  });

  function load() {
    client.get("/api/menu/owner").then((res) => setMenus(res.data));
  }

  useEffect(() => {
    client
      .get("/api/menu/owner")
      .then((res) => setMenus(res.data))
      .finally(() => setLoading(false));
  }, []);

  function updateItem(idx, field, value) {
    setForm((f) => {
      const items = f.items.map((it, i) => (i === idx ? { ...it, [field]: value } : it));
      return { ...f, items };
    });
  }

  async function createMenu(e) {
    e.preventDefault();
    setError("");
    try {
      await client.post("/api/menu/owner", {
        ...form,
        cutoffTime: form.cutoffTime.length === 5 ? `${form.cutoffTime}:00` : form.cutoffTime,
        items: form.items
          .filter((it) => it.name && it.price)
          .map((it) => ({ ...it, price: Number(it.price) })),
      });
      setForm({
        date: new Date().toISOString().slice(0, 10),
        mealType: "LUNCH",
        description: "",
        cutoffTime: "11:00",
        items: [emptyItem()],
      });
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not create menu");
    }
  }

  async function toggle(menu) {
    const action = menu.status === "OPEN" ? "close" : "open";
    await client.put(`/api/menu/owner/${menu.id}/${action}`);
    load();
  }

  return (
    <div>
      <h1 className="mb-4 text-2xl font-bold">Menu Management</h1>

      <form onSubmit={createMenu} className="card mb-6 space-y-4">
        <h2 className="font-semibold">Create a menu</h2>
        {error && (
          <p className="rounded bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>
        )}
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          <div>
            <label className="label">Date</label>
            <input
              type="date"
              className="input"
              value={form.date}
              onChange={(e) => setForm({ ...form, date: e.target.value })}
              required
            />
          </div>
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
            <label className="label">Cutoff</label>
            <input
              type="time"
              className="input"
              value={form.cutoffTime}
              onChange={(e) => setForm({ ...form, cutoffTime: e.target.value })}
              required
            />
          </div>
          <div>
            <label className="label">Description</label>
            <input
              className="input"
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
          </div>
        </div>

        <div className="space-y-2">
          <label className="label">Items</label>
          {form.items.map((it, idx) => (
            <div key={idx} className="flex gap-2">
              <input
                className="input flex-1"
                placeholder="Name"
                value={it.name}
                onChange={(e) => updateItem(idx, "name", e.target.value)}
              />
              <input
                className="input w-28"
                type="number"
                step="0.01"
                placeholder="Price"
                value={it.price}
                onChange={(e) => updateItem(idx, "price", e.target.value)}
              />
            </div>
          ))}
          <button
            type="button"
            className="btn-secondary"
            onClick={() => setForm({ ...form, items: [...form.items, emptyItem()] })}
          >
            + Add item
          </button>
        </div>

        <button className="btn-primary">Create menu</button>
      </form>

      {loading && <p className="text-stone-500">Loading…</p>}
      <div className="space-y-4">
        {menus.map((menu) => (
          <div key={menu.id} className="card">
            <div className="mb-2 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="font-semibold">
                  {menu.date} · {menu.mealType}
                </span>
                <StatusBadge status={menu.status} />
              </div>
              <button className="btn-secondary" onClick={() => toggle(menu)}>
                {menu.status === "OPEN" ? "Close" : "Open"}
              </button>
            </div>
            <ul className="text-sm text-stone-600">
              {menu.items.map((it) => (
                <li key={it.id} className="flex justify-between">
                  <span>{it.name}</span>
                  <span>₹{it.price}</span>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
    </div>
  );
}
