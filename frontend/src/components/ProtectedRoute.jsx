import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/**
 * Guards routes by authentication and (optionally) role. Roles come from the JWT-derived
 * user stored in AuthContext; the real enforcement is still server-side at the gateway.
 */
export default function ProtectedRoute({ children, role }) {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  if (role && user.role !== role) {
    return <Navigate to="/" replace />;
  }
  return children;
}
