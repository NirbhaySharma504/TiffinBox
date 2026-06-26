import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";

export default function Navbar() {
  const { isAuthenticated, isOwner, user, logout } = useAuth();
  const { count } = useCart();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  const linkClass = ({ isActive }) =>
    `px-3 py-2 text-sm font-medium rounded-md ${
      isActive ? "bg-brand/10 text-brand" : "text-stone-600 hover:text-brand"
    }`;

  return (
    <nav className="sticky top-0 z-10 border-b border-stone-200 bg-white">
      <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-3">
        <Link to="/" className="text-lg font-bold text-brand">
          🍱 TiffinBox
        </Link>

        <div className="flex items-center gap-1">
          {!isOwner && (
            <>
              <NavLink to="/" className={linkClass} end>
                Menu
              </NavLink>
              {isAuthenticated && (
                <>
                  <NavLink to="/orders" className={linkClass}>
                    My Orders
                  </NavLink>
                  <NavLink to="/subscriptions" className={linkClass}>
                    Subscriptions
                  </NavLink>
                  <NavLink to="/cart" className={linkClass}>
                    Cart{count > 0 ? ` (${count})` : ""}
                  </NavLink>
                </>
              )}
            </>
          )}

          {isOwner && (
            <>
              <NavLink to="/owner/orders" className={linkClass}>
                Orders
              </NavLink>
              <NavLink to="/owner/menu" className={linkClass}>
                Menus
              </NavLink>
              <NavLink to="/owner/payments" className={linkClass}>
                Payments
              </NavLink>
              <NavLink to="/owner/subscriptions" className={linkClass}>
                Subscriptions
              </NavLink>
            </>
          )}

          {isAuthenticated ? (
            <div className="ml-2 flex items-center gap-2">
              <span className="hidden text-sm text-stone-500 sm:inline">
                {user.name} · {user.role}
              </span>
              <button onClick={handleLogout} className="btn-secondary">
                Logout
              </button>
            </div>
          ) : (
            <>
              <NavLink to="/login" className={linkClass}>
                Login
              </NavLink>
              <NavLink to="/register" className="btn-primary ml-1">
                Sign up
              </NavLink>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
