#!/usr/bin/env bash
#
# Stops all TiffinBox backend services started by run-backend.sh.
# (Leaves the Postgres/Kafka Docker containers running.)
#
echo "Stopping all spring-boot:run processes ..."
pkill -f "spring-boot:run" 2>/dev/null && echo "stopped." || echo "nothing running."
