#!/usr/bin/env bash
set -euo pipefail

HOST="${INFLUX_BACKUP_HOST:-127.0.0.1:8088}"
DATABASE="${INFLUX_DATABASE:-allst_iot}"
RETENTION="${INFLUX_RETENTION:-rp_raw_7d}"
TARGET="${1:-./data/influx-backup/portable-$(date -u +%Y%m%dT%H%M%SZ)}"

mkdir -p "$TARGET"
influxd backup -portable -database "$DATABASE" -retention "$RETENTION" -host "$HOST" "$TARGET"
echo "$TARGET"
