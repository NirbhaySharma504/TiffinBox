const COLORS = {
  PLACED: "bg-blue-100 text-blue-700",
  PREPARING: "bg-amber-100 text-amber-700",
  OUT_FOR_DELIVERY: "bg-purple-100 text-purple-700",
  DELIVERED: "bg-green-100 text-green-700",
  CANCELLED: "bg-red-100 text-red-700",
  ACTIVE: "bg-green-100 text-green-700",
  PAUSED: "bg-amber-100 text-amber-700",
  PAID: "bg-green-100 text-green-700",
  PENDING: "bg-amber-100 text-amber-700",
  FAILED: "bg-red-100 text-red-700",
  OPEN: "bg-green-100 text-green-700",
  CLOSED: "bg-stone-200 text-stone-600",
  POSITIVE: "bg-green-100 text-green-700",
  NEUTRAL: "bg-blue-100 text-blue-700",
  NEGATIVE: "bg-red-100 text-red-700",
  UNKNOWN: "bg-stone-200 text-stone-600",
};

export default function StatusBadge({ status }) {
  return (
    <span className={`badge ${COLORS[status] || "bg-stone-100 text-stone-600"}`}>
      {String(status).replaceAll("_", " ")}
    </span>
  );
}
