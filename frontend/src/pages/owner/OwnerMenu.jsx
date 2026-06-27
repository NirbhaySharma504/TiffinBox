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
          <MenuCard key={menu.id} menu={menu} onChanged={load} />
        ))}
      </div>
    </div>
  );
}

/** A single menu row. OPEN menus can be edited inline; CLOSED ones are read-only. */
function MenuCard({ menu, onChanged }) {
  const [editing, setEditing] = useState(false);
  const [meta, setMeta] = useState({
    description: menu.description || "",
    cutoffTime: (menu.cutoffTime || "").slice(0, 5),
  });
  const [newItem, setNewItem] = useState(emptyItem());
  const [error, setError] = useState("");

  const isOpen = menu.status === "OPEN";

  async function call(fn) {
    setError("");
    try {
      await fn();
      onChanged();
    } catch (err) {
      setError(err.response?.data?.message || "Action failed");
    }
  }

  const toggleStatus = () =>
    call(() =>
      client.put(`/api/menu/owner/${menu.id}/${isOpen ? "close" : "open"}`)
    );

  const saveMeta = () =>
    call(() =>
      client.put(`/api/menu/owner/${menu.id}`, {
        description: meta.description,
        cutoffTime: meta.cutoffTime.length === 5 ? `${meta.cutoffTime}:00` : meta.cutoffTime,
      })
    );

  const saveItem = (item, price, available) =>
    call(() =>
      client.put(`/api/menu/owner/items/${item.id}`, { price: Number(price), available })
    );

  const deleteItem = (item) =>
    call(() => client.delete(`/api/menu/owner/items/${item.id}`));

  const addItem = () =>
    call(async () => {
      await client.post(`/api/menu/owner/${menu.id}/items`, {
        name: newItem.name,
        price: Number(newItem.price),
        description: newItem.description || null,
      });
      setNewItem(emptyItem());
    });

  return (
    <div className="card">
      <div className="mb-2 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className="font-semibold">
            {menu.date} · {menu.mealType}
          </span>
          <StatusBadge status={menu.status} />
          <span className="text-sm text-stone-400">cutoff {menu.cutoffTime}</span>
        </div>
        <div className="flex gap-2">
          {isOpen && (
            <button className="btn-secondary" onClick={() => setEditing((e) => !e)}>
              {editing ? "Done" : "Edit"}
            </button>
          )}
          <button className="btn-secondary" onClick={toggleStatus}>
            {isOpen ? "Close" : "Open"}
          </button>
        </div>
      </div>

      {error && (
        <p className="mb-2 rounded bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>
      )}

      {/* Read-only view */}
      {!editing && (
        <ul className="text-sm text-stone-600">
          {menu.items.map((it) => (
            <li key={it.id} className="flex justify-between">
              <span className={it.available ? "" : "text-stone-400 line-through"}>
                {it.name}
                {!it.available && " (unavailable)"}
              </span>
              <span>₹{it.price}</span>
            </li>
          ))}
        </ul>
      )}

      {/* Edit view (OPEN menus only) */}
      {editing && isOpen && (
        <div className="space-y-4">
          <div className="flex flex-wrap items-end gap-3 border-b border-stone-100 pb-3">
            <div className="flex-1">
              <label className="label">Description</label>
              <input
                className="input"
                value={meta.description}
                onChange={(e) => setMeta({ ...meta, description: e.target.value })}
              />
            </div>
            <div>
              <label className="label">Cutoff</label>
              <input
                type="time"
                className="input"
                value={meta.cutoffTime}
                onChange={(e) => setMeta({ ...meta, cutoffTime: e.target.value })}
              />
            </div>
            <button className="btn-primary" onClick={saveMeta}>
              Save details
            </button>
          </div>

          <div className="space-y-2">
            {menu.items.map((it) => (
              <ItemEditor
                key={it.id}
                item={it}
                onSave={saveItem}
                onDelete={deleteItem}
              />
            ))}
          </div>

          <div className="flex gap-2 border-t border-stone-100 pt-3">
            <input
              className="input flex-1"
              placeholder="New item name"
              value={newItem.name}
              onChange={(e) => setNewItem({ ...newItem, name: e.target.value })}
            />
            <input
              className="input w-28"
              type="number"
              step="0.01"
              placeholder="Price"
              value={newItem.price}
              onChange={(e) => setNewItem({ ...newItem, price: e.target.value })}
            />
            <button
              className="btn-primary"
              disabled={!newItem.name || !newItem.price}
              onClick={addItem}
            >
              Add
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

function ItemEditor({ item, onSave, onDelete }) {
  const [price, setPrice] = useState(item.price);
  const [available, setAvailable] = useState(item.available);

  return (
    <div className="flex items-center gap-2">
      <span className="flex-1">{item.name}</span>
      <input
        className="input w-28"
        type="number"
        step="0.01"
        value={price}
        onChange={(e) => setPrice(e.target.value)}
      />
      <label className="flex items-center gap-1 text-sm text-stone-600">
        <input
          type="checkbox"
          checked={available}
          onChange={(e) => setAvailable(e.target.checked)}
        />
        available
      </label>
      <button className="btn-secondary" onClick={() => onSave(item, price, available)}>
        Save
      </button>
      <button
        className="btn border border-red-300 text-red-600 hover:bg-red-50"
        onClick={() => onDelete(item)}
      >
        Delete
      </button>
    </div>
  );
}
