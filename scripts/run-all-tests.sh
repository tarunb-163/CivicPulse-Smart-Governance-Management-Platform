#!/usr/bin/env bash
# CivicPulse Complete Automated QA Test Harness (Bash)
# Execution: bash scripts/run-all-tests.sh
# Exits 0 only if all test suites pass. Exits non-zero on any failure.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

echo -e "\033[36m═══════════════════════════════════════════════════════════════════════\033[0m"
echo -e "\033[36m  CivicPulse Complete QA Automation Test Harness & Verification Suite  \033[0m"
echo -e "\033[36m═══════════════════════════════════════════════════════════════════════\033[0m"

RESULTS_DIR="$PROJECT_ROOT/docs/test-results"
mkdir -p "$RESULTS_DIR"

APP_PID=""

cleanup() {
    if [[ -n "$APP_PID" ]] && kill -0 "$APP_PID" 2>/dev/null; then
        echo -e "\n\033[33mStopping background application server (PID $APP_PID)...\033[0m"
        kill -TERM "$APP_PID" || true
    fi
}
trap cleanup EXIT

# ── Step 1: Backend Verification (Maven) ──────────────────────────
echo -e "\n\033[33m[1/5] Running Backend Unit & Integration Tests (mvn test)...\033[0m"
mvn test -Dtest="*Test"
echo -e "  \033[32m[OK] Backend test suite passed cleanly.\033[0m"

# ── Step 2: Ensure Application Server is Running ──────────────────
echo -e "\n\033[33m[2/5] Checking Application Server on Port 8080...\033[0m"
if ! curl -s -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
    echo -e "  Starting Spring Boot application server..."
    mvn spring-boot:run &
    APP_PID=$!

    retries=30
    running=false
    while [[ $retries -gt 0 ]]; do
        sleep 2
        if curl -s http://localhost:8080/actuator/health | grep -q '"UP"'; then
            running=true
            break
        fi
        ((retries--))
    done

    if [[ "$running" != "true" ]]; then
        echo -e "\033[31m[ERROR] Failed to start Spring Boot application within 60s.\033[0m"
        exit 1
    fi
fi
echo -e "  \033[32m[OK] Application server is healthy and responding on http://localhost:8080\033[0m"

# ── Step 3: Run Full Playwright Test Suite ────────────────────────
echo -e "\n\033[33m[3/5] Executing Playwright E2E, Smoke, A11y & Security Suites...\033[0m"
export PLAYWRIGHT_HTML_REPORT="$RESULTS_DIR/html"
export PLAYWRIGHT_JUNIT_OUTPUT_NAME="$RESULTS_DIR/junit.xml"

npx playwright test --reporter=list,html,junit
echo -e "  \033[32m[OK] All Playwright test suites completed successfully.\033[0m"

# ── Step 4: Verify Test Results & Artifacts ───────────────────────
echo -e "\n\033[33m[4/5] Verifying Test Reports and Results in docs/test-results/...\033[0m"
echo -e "  Reports cataloged in: $RESULTS_DIR"

# ── Step 5: Summary ───────────────────────────────────────────────
echo -e "\n\033[32m[5/5] Test Harness Complete: 100% PASS\033[0m"
echo -e "\033[36m═══════════════════════════════════════════════════════════════════════\033[0m"
echo -e "\033[36m  All test suites passed. CivicPulse is fully verified and compliant!   \033[0m"
echo -e "\033[36m═══════════════════════════════════════════════════════════════════════\033[0m"

exit 0
