# ==============================================================================
# Student Central — Stop All Services Script (PowerShell)
# ==============================================================================

Write-Host "Stopping all Student Central services..." -ForegroundColor Yellow

$ports = @(8761, 8080, 8081, 8082, 8083, 8084, 8085, 8086, 8087, 8088, 5173)

foreach ($port in $ports) {
    $connections = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Where-Object { $_.State -eq 'Listen' }
    if ($connections) {
        $pids = $connections | Select-Object -ExpandProperty OwningProcess -Unique
        foreach ($pidToStop in $pids) {
            try {
                $proc = Get-Process -Id $pidToStop -ErrorAction SilentlyContinue
                if ($proc) {
                    Write-Host "Stopping $($proc.ProcessName) (PID: $pidToStop) on port $port..." -ForegroundColor Cyan
                    Stop-Process -Id $pidToStop -Force -ErrorAction SilentlyContinue
                }
            } catch {
                Write-Host "Could not stop PID $pidToStop : $_" -ForegroundColor Red
            }
        }
    }
}

Write-Host "All Student Central services stopped." -ForegroundColor Green
