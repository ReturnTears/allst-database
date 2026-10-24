#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "用法: INFLUX_NEW_DATABASE=allst_iot_restore ./restore.sh <backup-dir>" >&2
  exit 1
fi

HOST="${INFLUX_BACKUP_HOST:-127.0.0.1:8088}"
DATABASE="${INFLUX_DATABASE:-allst_iot}"
RETENTION="${INFLUX_RETENTION:-rp_raw_7d}"
NEW_DATABASE="${INFLUX_NEW_DATABASE:-allst_iot_restore}"

if [[ "$NEW_DATABASE" == "$DATABASE" ]]; then
  echo "拒绝覆盖正在使用的库。把 INFLUX_NEW_DATABASE 设成一个新库名。" >&2
  exit 1
fi

influxd restore -portable -db "$DATABASE" -rp "$RETENTION" -newdb "$NEW_DATABASE" -host "$HOST" "$1"
echo "已恢复到 $NEW_DATABASE"
