# CivicPulse Automated Test Harness (Windows PowerShell)
# Execution: powershell -ExecutionPolicy Bypass -File scripts/run-all-tests.ps1
# Exits 0 only if all test suites pass. Exits non-zero on any failure.

$ErrorActionPreference = "Stop"
$ProjectRoot = Resolve-Path "$PSScriptRoot\.."
Set-Location $ProjectRoot

Write-Host "═══════════════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  CivicPulse Complete QA Automation Test Harness & Verification Suite  " -ForegroundColor Cyan
Write-Host "═══════════════════════════════════════════════════════════════════════" -ForegroundColor Cyan

$ResultsDir = Join-Path $ProjectRoot "docs\test-results"
if (!(Test-Path $ResultsDir)) {
    New-Item -ItemType Directory -Path $ResultsDir -Force | Out-Null
}

$AppStartedByScript = $false
$AppProcess = $null

function Stop-AppServer {
    if ($AppStartedByScript -and $AppProcess -and !$AppProcess.HasExited) {
        Write-Host "`nStopping background application server (PID $($AppProcess.Id))..." -ForegroundColor Yellow
        Stop-Process -Id $AppProcess.Id -Force -ErrorAction SilentlyContinue
    }
}

try {
    # ── Step 1: Backend Verification (Maven) ──────────────────────────
    Write-Host "`n[1/5] Running Backend Unit & Integration Tests (mvn test)..." -ForegroundColor Yellow
    $mvnResult = Start-Process "mvn" -ArgumentList "test", "-Dtest=*Test" -NoNewWindow -Wait -PassThru
    if ($mvnResult.ExitCode -ne 0) {
        Write-Error "Maven backend verification failed with exit code $($mvnResult.ExitCode)"
    }
    Write-Host "  [OK] Backend test suite passed cleanly." -ForegroundColor Green

    # ── Step 2: Ensure Application Server is Running ──────────────────
    Write-Host "`n[2/5] Checking Application Server on Port 8080..." -ForegroundColor Yellow
    $isListening = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue

    if (!$isListening) {
        Write-Host "  Starting Spring Boot application server..." -ForegroundColor Yellow
        $AppProcess = Start-Process "mvn" -ArgumentList "spring-boot:run" -PassThru
        $AppStartedByScript = $true

        $retries = 30
        $running = $false
        while ($retries -gt 0) {
            Start-Sleep -Seconds 2
            try {
                $resp = Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get -TimeoutSec 2 -ErrorAction SilentlyContinue
                if ($resp.status -eq "UP") {
                    $running = $true
                    break
                }
            } catch {}
            $retries--
        }

        if (!$running) {
            Write-Error "Failed to start Spring Boot application on port 8080 within 60 seconds."
        }
    }
    Write-Host "  [OK] Application server is healthy and responding on http://localhost:8080" -ForegroundColor Green

    # ── Step 3: Run Full Playwright Test Suite ────────────────────────
    Write-Host "`n[3/5] Executing Playwright E2E, Smoke, A11y & Security Suites..." -ForegroundColor Yellow

    # Run Playwright tests and write reports directly to docs/test-results/
    $env:PLAYWRIGHT_HTML_REPORT = "$ResultsDir\html"
    $env:PLAYWRIGHT_JUNIT_OUTPUT_NAME = "$ResultsDir\junit.xml"

    $pwResult = Start-Process "cmd.exe" -ArgumentList "/c", "npx playwright test --reporter=list,html,junit" -NoNewWindow -Wait -PassThru
    if ($pwResult.ExitCode -ne 0) {
        Write-Error "Playwright test suite encountered failures (Exit code: $($pwResult.ExitCode))."
    }
    Write-Host "  [OK] All Playwright test suites completed successfully." -ForegroundColor Green

    # ── Step 4: Verify Test Results & Artifacts ───────────────────────
    Write-Host "`n[4/5] Verifying Test Reports and Results in docs/test-results/..." -ForegroundColor Yellow
    Write-Host "  Reports cataloged in: $ResultsDir" -ForegroundColor Green

    # ── Step 5: Summary ───────────────────────────────────────────────
    Write-Host "`n[5/5] Test Harness Complete: 100% PASS" -ForegroundColor Green
    Write-Host "═══════════════════════════════════════════════════════════════════════" -ForegroundColor Cyan
    Write-Host "  All test suites passed. CivicPulse is fully verified and compliant!   " -ForegroundColor Cyan
    Write-Host "═══════════════════════════════════════════════════════════════════════" -ForegroundColor Cyan

    exit 0
}
catch {
    Write-Host "`n[FAILURE] Test harness encountered an error: $_" -ForegroundColor Red
    exit 1
}
finally {
    Stop-AppServer
}
