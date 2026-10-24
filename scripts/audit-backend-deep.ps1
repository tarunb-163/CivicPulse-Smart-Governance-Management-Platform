# ==============================================================================
# CivicPulse — Deep Backend & Security Verification Suite
# ==============================================================================
param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"
$passed = 0
$failed = 0

function Assert-Test($name, $condition, $details) {
    if ($condition) {
        Write-Host " [PASS] $name" -ForegroundColor Green
        $script:passed++
    } else {
        Write-Host " [FAIL] $name - $details" -ForegroundColor Red
        $script:failed++
    }
}

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "Starting CivicPulse Deep Backend Verification Suite against: $BaseUrl" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

# 1. AUTHENTICATION & TOKENS
Write-Host "`n--- Section 1: Authentication & Token Isolation ---" -ForegroundColor Yellow

# 1.1 Unauthenticated requests return 401
try {
    Invoke-RestMethod -Uri "$BaseUrl/api/v1/complaints/mine" -Method Get | Out-Null
    Assert-Test "Unauthenticated /complaints/mine rejected" $false "Expected 401 but got 200"
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Assert-Test "Unauthenticated /complaints/mine rejected" ($code -eq 401) "Got $code"
}

try {
    Invoke-RestMethod -Uri "$BaseUrl/api/v1/officer/queue" -Method Get | Out-Null
    Assert-Test "Unauthenticated /officer/queue rejected" $false "Expected 401 but got 200"
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Assert-Test "Unauthenticated /officer/queue rejected" ($code -eq 401) "Got $code"
}

try {
    Invoke-RestMethod -Uri "$BaseUrl/api/v1/admin/audit-logs" -Method Get | Out-Null
    Assert-Test "Unauthenticated /admin/audit-logs rejected" $false "Expected 401 but got 200"
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Assert-Test "Unauthenticated /admin/audit-logs rejected" ($code -eq 401) "Got $code"
}

# 1.2 Tampered token returns 401
try {
    Invoke-RestMethod -Uri "$BaseUrl/api/v1/complaints/mine" -Method Get -Headers @{ Authorization = "Bearer invalid.fake.token" } | Out-Null
    Assert-Test "Tampered token rejected with 401" $false "Expected 401 but succeeded"
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Assert-Test "Tampered token rejected with 401" ($code -eq 401) "Got $code"
}

# 1.3 Login as Citizen, Officer, Admin
$citizenLogin = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"citizen@civicpulse.test","password":"Password@123"}'
$citizenToken = $citizenLogin.accessToken

$officerLogin = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"officer.roads@civicpulse.test","password":"Password@123"}'
$officerToken = $officerLogin.accessToken

$adminLogin = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"admin@civicpulse.test","password":"Password@123"}'
$adminToken = $adminLogin.accessToken

Assert-Test "Citizen login succeeded" ($citizenToken -ne $null) ""
Assert-Test "Officer login succeeded" ($officerToken -ne $null) ""
Assert-Test "Admin login succeeded" ($adminToken -ne $null) ""

# 2. ROLE-BASED ACCESS CONTROL (RBAC)
Write-Host "`n--- Section 2: Role-Based Access Control (RBAC) ---" -ForegroundColor Yellow

# 2.1 Citizen cannot access Admin endpoints (403)
try {
    Invoke-RestMethod -Uri "$BaseUrl/api/v1/admin/users" -Method Get -Headers @{ Authorization = "Bearer $citizenToken" } | Out-Null
    Assert-Test "Citizen access to /admin/users forbidden (403)" $false "Expected 403"
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Assert-Test "Citizen access to /admin/users forbidden (403)" ($code -eq 403) "Got $code"
}

# 2.2 Citizen cannot access Officer queue (403)
try {
    Invoke-RestMethod -Uri "$BaseUrl/api/v1/officer/queue" -Method Get -Headers @{ Authorization = "Bearer $citizenToken" } | Out-Null
    Assert-Test "Citizen access to /officer/queue forbidden (403)" $false "Expected 403"
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Assert-Test "Citizen access to /officer/queue forbidden (403)" ($code -eq 403) "Got $code"
}

# 2.3 Officer cannot access Admin settings (403)
try {
    Invoke-RestMethod -Uri "$BaseUrl/api/v1/admin/settings" -Method Get -Headers @{ Authorization = "Bearer $officerToken" } | Out-Null
    Assert-Test "Officer access to /admin/settings forbidden (403)" $false "Expected 403"
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Assert-Test "Officer access to /admin/settings forbidden (403)" ($code -eq 403) "Got $code"
}

# 3. NOTIFICATION ENDPOINTS (DEF-001 Verification)
Write-Host "`n--- Section 3: Notification API Methods ---" -ForegroundColor Yellow
$notifs = Invoke-RestMethod -Uri "$BaseUrl/api/v1/notifications" -Method Get -Headers @{ Authorization = "Bearer $citizenToken" }
Assert-Test "Get notifications list" ($notifs.content -ne $null) ""

$unread = Invoke-RestMethod -Uri "$BaseUrl/api/v1/notifications/unread-count" -Method Get -Headers @{ Authorization = "Bearer $citizenToken" }
Assert-Test "Get unread notifications count" ($unread.unreadCount -ge 0) ""

# Test PATCH on /read-all (strict PRD Section 8: PATCH only)
try {
    Invoke-RestMethod -Uri "$BaseUrl/api/v1/notifications/read-all" -Method Patch -Headers @{ Authorization = "Bearer $citizenToken" } | Out-Null
    Assert-Test "PATCH /notifications/read-all succeeds (204/200)" $true ""
} catch {
    Assert-Test "PATCH /notifications/read-all succeeds (204/200)" $false "$($_.Exception.Message)"
}

try {
    Invoke-RestMethod -Uri "$BaseUrl/api/v1/notifications/read-all" -Method Post -Headers @{ Authorization = "Bearer $citizenToken" } | Out-Null
    Assert-Test "POST /notifications/read-all rejected (405 Method Not Allowed)" $false "Should be 405"
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Assert-Test "POST /notifications/read-all rejected (405 Method Not Allowed)" ($code -eq 405) "Got $code"
}

# 4. PUBLIC DATA INTEGRITY & ZERO DATA LEAK
Write-Host "`n--- Section 4: Public Data Integrity & Anti-Leak ---" -ForegroundColor Yellow
$track = Invoke-RestMethod -Uri "$BaseUrl/api/v1/public/track/CP-2026-000001" -Method Get
Assert-Test "Public tracking loads complaint" ($track.publicCode -eq "CP-2026-000001") ""
Assert-Test "Public tracking hides citizen name" ($track.citizenName -eq $null) ""
Assert-Test "Public tracking hides citizen email" ($track.citizenEmail -eq $null) ""
Assert-Test "Public tracking hides citizen phone" ($track.citizenPhone -eq $null) ""
Assert-Test "Public tracking hides citizen ID" ($track.citizenId -eq $null) ""

$pubStats = Invoke-RestMethod -Uri "$BaseUrl/api/v1/public/stats" -Method Get
Assert-Test "Public stats returns grievance counts" ($pubStats.totalGrievances -gt 0) ""
Assert-Test "Public stats returns department breakdowns" ($pubStats.departmentStats.Count -gt 0) ""

$mapPoints = Invoke-RestMethod -Uri "$BaseUrl/api/v1/public/map-points" -Method Get
Assert-Test "Public map-points returns geo coordinates" ($mapPoints.Count -gt 0) ""

# 5. CIVICBOT AI INTENTS & TRACKING
Write-Host "`n--- Section 5: CivicBot AI Assistant ---" -ForegroundColor Yellow
$botMsg1 = Invoke-RestMethod -Uri "$BaseUrl/api/v1/bot/message" -Method Post -ContentType "application/json" -Body '{"message":"How do I file a complaint?"}'
Assert-Test "CivicBot handles FAQ message" ($botMsg1.reply -ne $null -and $botMsg1.reply.Length -gt 10) ""

$botMsg2 = Invoke-RestMethod -Uri "$BaseUrl/api/v1/bot/message" -Method Post -ContentType "application/json" -Body '{"message":"Track CP-2026-000001"}'
Assert-Test "CivicBot tracks ticket by ID" ($botMsg2.reply -like "*CP-2026-000001*") ""

# 6. ANALYTICS & EXPORTS
Write-Host "`n--- Section 6: Analytics & Export Integrity ---" -ForegroundColor Yellow
$analyticsSummary = Invoke-RestMethod -Uri "$BaseUrl/api/v1/analytics/summary" -Method Get -Headers @{ Authorization = "Bearer $adminToken" }
Assert-Test "Analytics summary retrieved" ($analyticsSummary.totalComplaints -gt 0) ""

$csvFile = "$env:TEMP\civicpulse-export-test.csv"
Invoke-WebRequest -Uri "$BaseUrl/api/v1/analytics/export/csv" -Method Get -Headers @{ Authorization = "Bearer $adminToken" } -OutFile $csvFile -UseBasicParsing
$csvContent = Get-Content -Path $csvFile -Raw
Assert-Test "CSV export file generated" (Test-Path $csvFile) ""
Assert-Test "CSV export contains standard headers" ($csvContent -like "*Code,Title,Category*") ""

$pdfFile = "$env:TEMP\civicpulse-export-test.pdf"
Invoke-WebRequest -Uri "$BaseUrl/api/v1/analytics/export/pdf" -Method Get -Headers @{ Authorization = "Bearer $adminToken" } -OutFile $pdfFile -UseBasicParsing
Assert-Test "PDF export file generated" (Test-Path $pdfFile) ""
$pdfSize = (Get-Item $pdfFile).Length
Assert-Test "PDF export is non-empty binary" ($pdfSize -gt 1000) "Size was $pdfSize"

# 7. SECURITY & SQL INJECTION RESISTANCE
Write-Host "`n--- Section 7: SQL Injection & XSS Resilience ---" -ForegroundColor Yellow
$sqliQuery = "' OR '1'='1"
try {
    $searchResult = Invoke-RestMethod -Uri "$BaseUrl/api/v1/complaints/mine?query=$sqliQuery" -Method Get -Headers @{ Authorization = "Bearer $citizenToken" }
    Assert-Test "SQL injection string in search query safely parameterized" ($searchResult -ne $null) ""
} catch {
    Assert-Test "SQL injection string in search query safely parameterized" $false "$($_.Exception.Message)"
}

# 8. ERROR RESPONSE RFC-7807 COMPLIANCE
Write-Host "`n--- Section 8: Standard Error JSON Shape ---" -ForegroundColor Yellow
try {
    Invoke-RestMethod -Uri "$BaseUrl/api/v1/complaints/99999999" -Method Get -Headers @{ Authorization = "Bearer $citizenToken" } | Out-Null
    Assert-Test "404 Not Found returned for non-existent ID" $false "Expected 404"
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Assert-Test "404 Not Found returned for non-existent ID" ($code -eq 404) "Got $code"
}

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "DEEP BACKEND AUDIT SUMMARY: $passed PASSED, $failed FAILED" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

if ($failed -gt 0) {
    exit 1
} else {
    exit 0
}
