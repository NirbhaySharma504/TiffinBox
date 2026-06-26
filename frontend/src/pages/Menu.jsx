import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import client from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

export default function Menu() {
  const { isAuthenticated } = useAuth();
  const { add, count } = useCart();
  const [menus, setMenus] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    // /api/menu/today is a public route at the gateway.
    client
      .get("/api/menu/today")
      .then((res) => setMenus(res.data))
      .catch(() => setError("Could not load today's menu"))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="text-stone-500">Loading menu…</p>;
  if (error) return <p className="text-red-600">{error}</p>;

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h1 className="text-2xl font-bold">Today's Menu</h1>
        {isAuthenticated && count > 0 && (
          <Link to="/cart" className="btn-primary">
            Go to cart ({count})
          </Link>
        )}
      </div>

      {menus.length === 0 && (
        <p className="text-stone-500">No open menus right now. Check back later!</p>
      )}

      <div className="space-y-6">
        {menus.map((menu) => (
          <div key={menu.id} className="card">
            <div className="mb-3 flex items-baseline justify-between">
              <h2 className="text-lg font-semibold">
                {menu.mealType} {menu.description ? `· ${menu.description}` : ""}
              </h2>
              <span className="text-sm text-stone-400">
                Order by {menu.cutoffTime}
              </span>
            </div>
            <ul className="divide-y divide-stone-100">
              {menu.items.map((item) => (
                <li key={item.id} className="flex items-center justify-between py-2">
                  <div>
                    <p className="font-medium">{item.name}</p>
                    {item.description && (
                      <p className="text-sm text-stone-500">{item.description}</p>
                    )}
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="font-semibold">₹{item.price}</span>
                    {isAuthenticated ? (
                      <button
                        className="btn-primary"
                        disabled={!item.available}
                        onClick={() => add(menu.id, item)}
                      >
                        {item.available ? "Add" : "Unavailable"}
                      </button>
                    ) : (
                      <Link to="/login" className="btn-secondary">
                        Login to order
                      </Link>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
    </div>
  );
}
