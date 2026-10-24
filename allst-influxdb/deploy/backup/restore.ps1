$ErrorActionPreference = "Stop"

if ($args.Count -lt 1) {
    Write-Error "用法: `$env:INFLUX_NEW_DATABASE='allst_iot_restore'; .\restore.ps1 <backup-dir>"
}

$hostAddr = if ($env:INFLUX_BACKUP_HOST) { $env:INFLUX_BACKUP_HOST } else { "127.0.0.1:8088" }
$database = if ($env:INFLUX_DATABASE) { $env:INFLUX_DATABASE } else { "allst_iot" }
$retention = if ($env:INFLUX_RETENTION) { $env:INFLUX_RETENTION } else { "rp_raw_7d" }
$newDatabase = if ($env:INFLUX_NEW_DATABASE) { $env:INFLUX_NEW_DATABASE } else { "allst_iot_restore" }

if ($newDatabase -eq $database) {
    Write-Error "拒绝覆盖正在使用的库。把 INFLUX_NEW_DATABASE 设成一个新库名。"
}

& influxd restore -portable -db $database -rp $retention -newdb $newDatabase -host $hostAddr $args[0]
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
Write-Output "已恢复到 $newDatabase"
