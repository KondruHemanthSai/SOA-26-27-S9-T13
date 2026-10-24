# ==============================================================================
# Student Central - Start All Services Script (PowerShell)
# ==============================================================================
param(
    [string]$Mode = ""
)

$ErrorActionPreference = "Continue"
$rootDir = $PSScriptRoot
if (-not $rootDir) { $rootDir = Get-Location }
Set-Location $rootDir

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "       Starting Student Central Microservices System       " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Load Environment Variables from .env
$envFile = Join-Path $rootDir ".env"
if (Test-Path $envFile) {
    Write-Host "[INFO] Loading environment variables from .env..." -ForegroundColor Green
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
            $parts = $line.Split("=", 2)
            $name = $parts[0].Trim()
            $value = $parts[1].Trim()
            [System.Environment]::SetEnvironmentVariable($name, $value, "Process")
        }
    }
    [System.Environment]::SetEnvironmentVariable("MAVEN_OPTS", "-Xmx384m", "Process")
} else {
    Write-Host "[WARN] .env file not found at $envFile!" -ForegroundColor Yellow
}

# 2. Ensure logs folder exists
$logsDir = Join-Path $rootDir "logs"
if (-not (Test-Path $logsDir)) {
    New-Item -ItemType Directory -Path $logsDir | Out-Null
}

# Helper to check if a port is in use
function Test-PortListening([int]$port) {
    $conn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Where-Object { $_.State -eq 'Listen' }
    return ($null -ne $conn)
}

# 3. Determine Startup Mode (Separate Windows vs Background)
if (-not $Mode) {
    Write-Host ""
    Write-Host "Select Startup Mode:" -ForegroundColor Yellow
    Write-Host "  [1] Separate Windows (Recommended: live logs and debug errors)" -ForegroundColor White
    Write-Host "  [2] Background Mode  (Silent: logs written to .\logs\)" -ForegroundColor White
    Write-Host "Auto-selecting [1] in 5 seconds..." -ForegroundColor Gray
    
    $choice = "1"
    try {
        if (-not [Console]::IsInputRedirected) {
            for ($i = 0; $i -lt 5; $i++) {
                if ([Console]::KeyAvailable) {
                    $key = [Console]::ReadKey($true).KeyChar
                    if ($key -eq '2') { $choice = "2"; break }
                    if ($key -eq '1') { $choice = "1"; break }
                }
                Start-Sleep -Seconds 1
            }
        }
    } catch {
        $choice = "1"
    }
    $Mode = $choice
}

$useWindows = ($Mode -eq "1" -or $Mode -eq "windows" -or $Mode -eq "-Visible")

if ($useWindows) {
    Write-Host "[MODE] Launching in Separate Console Windows (Live Logs)" -ForegroundColor Green
} else {
    Write-Host "[MODE] Launching in Background Mode (Logs in .\logs\)" -ForegroundColor Green
}

# Helper to start a Spring Boot service
function Start-SpringBootService([string]$serviceName, [int]$port) {
    if (Test-PortListening $port) {
        Write-Host "[$serviceName] Already running on port $port." -ForegroundColor Yellow
        return
    }

    Write-Host "[$serviceName] Starting on port $port..." -ForegroundColor Green
    $mvnArgs = @(
        "--projects", $serviceName,
        "spring-boot:run"
    )

    if ($useWindows) {
        Start-Process -FilePath "cmd.exe" `
                      -ArgumentList ("/k title $serviceName && mvn " + ($mvnArgs -join " ")) `
                      -WorkingDirectory $rootDir
    } else {
        $stdoutLog = Join-Path $logsDir "$serviceName.log"
        $stderrLog = Join-Path $logsDir "$serviceName-err.log"
        Start-Process -FilePath "cmd.exe" `
                      -ArgumentList ("/c mvn " + ($mvnArgs -join " ")) `
                      -WorkingDirectory $rootDir `
                      -RedirectStandardOutput $stdoutLog `
                      -RedirectStandardError $stderrLog `
                      -WindowStyle Hidden
    }
}

# 4. Start Eureka Server First
Write-Host "`n--- Step 1: Starting Eureka Server ---" -ForegroundColor Magenta
Start-SpringBootService "eureka-server" 8761

# Wait for Eureka Server
$timeoutSec = 45
$elapsed = 0
Write-Host "Waiting for Eureka Server to listen on port 8761..." -NoNewline
while (-not (Test-PortListening 8761) -and ($elapsed -lt $timeoutSec)) {
    Start-Sleep -Seconds 2
    $elapsed += 2
    Write-Host "." -NoNewline
}
if (Test-PortListening 8761) {
    Write-Host " Ready!" -ForegroundColor Green
} else {
    Write-Host " Warning: Timed out waiting for Eureka. Continuing..." -ForegroundColor Yellow
}

# 5. Start Core Business Microservices
Write-Host "`n--- Step 2: Starting Microservices ---" -ForegroundColor Magenta
$services = @(
    @{ Name = "auth-service"; Port = 8081 },
    @{ Name = "student-service"; Port = 8082 },
    @{ Name = "admission-service"; Port = 8083 },
    @{ Name = "course-service"; Port = 8084 },
    @{ Name = "registration-service"; Port = 8085 },
    @{ Name = "schedule-service"; Port = 8086 },
    @{ Name = "notification-service"; Port = 8087 },
    @{ Name = "ai-service"; Port = 8088 }
)

foreach ($svc in $services) {
    Start-SpringBootService $svc.Name $svc.Port
    Start-Sleep -Milliseconds 600
}

# 6. Start API Gateway
Write-Host "`n--- Step 3: Starting API Gateway ---" -ForegroundColor Magenta
Start-SpringBootService "api-gateway" 8080

# 7. Start Frontend
Write-Host "`n--- Step 4: Starting Frontend (Vite) ---" -ForegroundColor Magenta
if (Test-PortListening 5173) {
    Write-Host "[frontend] Already running on port 5173." -ForegroundColor Yellow
} else {
    $frontendDir = Join-Path $rootDir "frontend"
    Write-Host "[frontend] Starting Vite Dev Server on port 5173..." -ForegroundColor Green
    if ($useWindows) {
        Start-Process -FilePath "cmd.exe" `
                      -ArgumentList "/c npm run dev" `
                      -WorkingDirectory $frontendDir
    } else {
        $feStdout = Join-Path $logsDir "frontend.log"
        $feStderr = Join-Path $logsDir "frontend-err.log"
        Start-Process -FilePath "cmd.exe" `
                      -ArgumentList "/c npm run dev" `
                      -WorkingDirectory $frontendDir `
                      -RedirectStandardOutput $feStdout `
                      -RedirectStandardError $feStderr `
                      -WindowStyle Hidden
    }
}

# Wait briefly for Frontend to listen
$feElapsed = 0
while (-not (Test-PortListening 5173) -and ($feElapsed -lt 15)) {
    Start-Sleep -Seconds 1
    $feElapsed += 1
}

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "             Student Central is Launching!                " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Frontend Application:   http://localhost:5173" -ForegroundColor Green
Write-Host "API Gateway:            http://localhost:8080" -ForegroundColor Green
Write-Host "Eureka Server:          http://localhost:8761" -ForegroundColor Green
if (-not $useWindows) {
    Write-Host "Logs Location:          $logsDir" -ForegroundColor Gray
}
Write-Host "Default Admin:          admin@studentcentral.com / Admin@12345" -ForegroundColor Yellow
Write-Host "Stop All Services:      .\stop-all.bat (or .\stop-all.ps1)" -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan

# Open browser
Start-Process "http://localhost:5173" -ErrorAction SilentlyContinue

try {
    if (-not [Console]::IsInputRedirected -and -not $Mode) {
        Write-Host "`nPress Enter to close this launcher window..." -ForegroundColor Gray
        $null = Read-Host
    }
} catch {}
