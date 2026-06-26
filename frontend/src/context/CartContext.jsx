import { createContext, useContext, useMemo, useState } from "react";

const CartContext = createContext(null);

/**
 * Cart holds items for a single menu at a time (you order from one menu/meal).
 * Switching to a different menu clears the cart.
 */
export function CartProvider({ children }) {
  const [menuId, setMenuId] = useState(null);
  const [lines, setLines] = useState({}); // menuItemId -> { item, quantity }

  function add(menuIdOfItem, item) {
    setMenuId((current) => {
      if (current && current !== menuIdOfItem) {
        setLines({}); // different menu -> reset
      }
      return menuIdOfItem;
    });
    setLines((prev) => {
      const existing = prev[item.id];
      const quantity = (existing?.quantity || 0) + 1;
      return { ...prev, [item.id]: { item, quantity } };
    });
  }

  function setQuantity(itemId, quantity) {
    setLines((prev) => {
      if (quantity <= 0) {
        const next = { ...prev };
        delete next[itemId];
        return next;
      }
      return { ...prev, [itemId]: { ...prev[itemId], quantity } };
    });
  }

  function clear() {
    setLines({});
    setMenuId(null);
  }

  const items = Object.values(lines);
  const total = useMemo(
    () => items.reduce((sum, l) => sum + Number(l.item.price) * l.quantity, 0),
    [items]
  );
  const count = items.reduce((n, l) => n + l.quantity, 0);

  return (
    <CartContext.Provider
      value={{ menuId, items, total, count, add, setQuantity, clear }}
    >
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error("useCart must be used within CartProvider");
  return ctx;
}
