$ErrorActionPreference = "Stop"

$hostAddr = if ($env:INFLUX_BACKUP_HOST) { $env:INFLUX_BACKUP_HOST } else { "127.0.0.1:8088" }
$database = if ($env:INFLUX_DATABASE) { $env:INFLUX_DATABASE } else { "allst_iot" }
$retention = if ($env:INFLUX_RETENTION) { $env:INFLUX_RETENTION } else { "rp_raw_7d" }
$stamp = [DateTime]::UtcNow.ToString("yyyyMMdd'T'HHmmss'Z'")
$target = if ($args.Count -ge 1) { $args[0] } else { Join-Path "data\influx-backup" "portable-$stamp" }

New-Item -ItemType Directory -Force -Path $target | Out-Null
& influxd backup -portable -database $database -retention $retention -host $hostAddr $target
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
Write-Output $target
