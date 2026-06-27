#!/usr/bin/env bash
#
# Starts the full TiffinBox backend (Eureka + gateway + all services) in the background.
# Logs go to scripts/logs/<service>.log. Use scripts/stop-backend.sh to stop everything.
#
# Prereqs: JDK 21, and the infra containers running:
#   docker start tiffinbox-postgres tiffinbox-kafka
#
set -euo pipefail

export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOGS="$ROOT/scripts/logs"
mkdir -p "$LOGS"

# Services that register with Eureka (started after Eureka is up).
SERVICES=(api-gateway user-service menu-service payment-service notification-service order-service subscription-service)

start() {
  local name="$1"
  echo "  starting $name ..."
  ( cd "$ROOT/$name" && nohup ./mvnw spring-boot:run > "$LOGS/$name.log" 2>&1 & )
}

wait_for() { # wait_for <url> <label>
  local url="$1" label="$2"
  for _ in $(seq 1 60); do
    if curl -fsS -o /dev/null "$url" 2>/dev/null; then echo "  $label is up"; return 0; fi
    sleep 2
  done
  echo "  WARNING: $label did not come up in time (check its log)"; return 1
}

echo "==> Checking infra containers"
docker start tiffinbox-postgres tiffinbox-kafka >/dev/null 2>&1 || true

echo "==> Starting Eureka"
start eureka-server
wait_for "http://localhost:8761/" "Eureka (:8761)"

echo "==> Starting services"
for svc in "${SERVICES[@]}"; do start "$svc"; done
wait_for "http://localhost:8080/api/menu/today" "API Gateway (:8080)"

echo ""
echo "All backend services launching. Watch them register at http://localhost:8761"
echo "Logs: $LOGS/<service>.log   |  Stop: scripts/stop-backend.sh"
