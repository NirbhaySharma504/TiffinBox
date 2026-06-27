import { Routes, Route, Navigate } from "react-router-dom";
import Navbar from "./components/Navbar";
import ProtectedRoute from "./components/ProtectedRoute";
import { useAuth } from "./context/AuthContext";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Menu from "./pages/Menu";
import Cart from "./pages/Cart";
import MyOrders from "./pages/MyOrders";
import MySubscriptions from "./pages/MySubscriptions";
import Feedback from "./pages/Feedback";

import OwnerOrders from "./pages/owner/OwnerOrders";
import OwnerMenu from "./pages/owner/OwnerMenu";
import OwnerPayments from "./pages/owner/OwnerPayments";
import OwnerSubscriptions from "./pages/owner/OwnerSubscriptions";
import OwnerFeedback from "./pages/owner/OwnerFeedback";

export default function App() {
  const { isOwner } = useAuth();

  return (
    <div className="min-h-screen">
      <Navbar />
      <main className="mx-auto max-w-5xl px-4 py-6">
        <Routes>
          {/* Home: owners see their dashboard, everyone else sees the menu */}
          <Route
            path="/"
            element={isOwner ? <Navigate to="/owner/orders" replace /> : <Menu />}
          />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Customer */}
          <Route
            path="/cart"
            element={
              <ProtectedRoute>
                <Cart />
              </ProtectedRoute>
            }
          />
          <Route
            path="/orders"
            element={
              <ProtectedRoute>
                <MyOrders />
              </ProtectedRoute>
            }
          />
          <Route
            path="/subscriptions"
            element={
              <ProtectedRoute>
                <MySubscriptions />
              </ProtectedRoute>
            }
          />
          <Route
            path="/feedback"
            element={
              <ProtectedRoute>
                <Feedback />
              </ProtectedRoute>
            }
          />

          {/* Owner */}
          <Route
            path="/owner/orders"
            element={
              <ProtectedRoute role="OWNER">
                <OwnerOrders />
              </ProtectedRoute>
            }
          />
          <Route
            path="/owner/menu"
            element={
              <ProtectedRoute role="OWNER">
                <OwnerMenu />
              </ProtectedRoute>
            }
          />
          <Route
            path="/owner/payments"
            element={
              <ProtectedRoute role="OWNER">
                <OwnerPayments />
              </ProtectedRoute>
            }
          />
          <Route
            path="/owner/subscriptions"
            element={
              <ProtectedRoute role="OWNER">
                <OwnerSubscriptions />
              </ProtectedRoute>
            }
          />
          <Route
            path="/owner/feedback"
            element={
              <ProtectedRoute role="OWNER">
                <OwnerFeedback />
              </ProtectedRoute>
            }
          />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </div>
  );
}
