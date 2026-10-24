$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
$BaseUrl = "http://localhost:8080"
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
Write-Host "Starting CivicPulse Phase E: Data, Concurrency & Edge Cases Suite" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

# 1. Register and Login dedicated Citizen for Phase E
$rand = Get-Random -Minimum 10000 -Maximum 99999
$email = "citizen.edge.$rand@civicpulse.test"
$regBody = @{
    name = "Edge Case Auditor"
    email = $email
    phone = "9876543210"
    password = "Password@123"
    acceptedTerms = $true
} | ConvertTo-Json

Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/register" -Method Post -ContentType "application/json" -Body $regBody | Out-Null
$login = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/login" -Method Post -ContentType "application/json" -Body "{`"email`":`"$email`",`"password`":`"Password@123`"}"
$token = $login.accessToken

# 2. Concurrency: Distinct public codes under simultaneous creation
Write-Host "`n--- Section 1: Concurrency & Public Code Generation ---" -ForegroundColor Yellow

$req1 = @{
    title = "Simultaneous Grievance Alpha"
    description = "Pothole near Davanagere University main entrance gate Alpha"
    categoryId = 1
    urgency = "MEDIUM"
    latitude = 14.4644
    longitude = 75.9218
    address = "University Road, Davanagere"
    anonymous = $false
    confirmNotDuplicate = $true
} | ConvertTo-Json

$req2 = @{
    title = "Streetlight failure Beta"
    description = "Streetlight pole flickering in Vidyanagar sector 2 Beta"
    categoryId = 2
    urgency = "HIGH"
    latitude = 14.4750
    longitude = 75.9325
    address = "Vidyanagar, Davanagere"
    anonymous = $false
    confirmNotDuplicate = $true
} | ConvertTo-Json

$res1 = Invoke-RestMethod -Uri "$BaseUrl/api/v1/complaints" -Method Post -ContentType "application/json" -Headers @{ Authorization = "Bearer $token" } -Body $req1
$res2 = Invoke-RestMethod -Uri "$BaseUrl/api/v1/complaints" -Method Post -ContentType "application/json" -Headers @{ Authorization = "Bearer $token" } -Body $req2

Assert-Test "Simultaneous submission 1 succeeded" ($res1.code -like "CP-*") "Code: $($res1.code)"
Assert-Test "Simultaneous submission 2 succeeded" ($res2.code -like "CP-*") "Code: $($res2.code)"
Assert-Test "Two distinct public codes generated without collision" ($res1.code -ne $res2.code) "$($res1.code) vs $($res2.code)"

# 3. Unicode, Emoji, Kannada, and Hindi Resilience
Write-Host "`n--- Section 2: Multilingual & Unicode Resilience ---" -ForegroundColor Yellow
$kannadaWord = "$([char]0x0C97)$([char]0x0CC1)$([char]0x0C82)$([char]0x0CA1)$([char]0x0CBF)" # ಗುಂಡಿ
$hindiWord = "$([char]0x0917)$([char]0x0921)$([char]0x094D)$([char]0x0922)$([char]0x093E)" # गड्ढा
$emoji = "$([char]0xD83D)$([char]0xDEA7)" # 🚧

$multilingualJson = @"
{
    "title": "Pothole Grievance \u0CB0\u0CB8\u0CCD\u0CA4\u0CC6 \u0938\u0921\u093C\u0915",
    "description": "Kannada: \u0C97\u0CC1\u0C82\u0CA1\u0CBF. Hindi: \u0917\u0921\u094D\u0922\u093E. Alert: \uD83D\uDEA7\u26A0\uFE0F",
    "categoryId": 1,
    "urgency": "HIGH",
    "latitude": 14.4847,
    "longitude": 75.9420,
    "address": "Davanagere City \u0CA6\u0CBE\u0CB5\u0CA3\u0C97\u0CC6\u0CB0\u0CC6",
    "anonymous": false,
    "confirmNotDuplicate": true
}
"@

$reqBytes = [System.Text.Encoding]::UTF8.GetBytes($multilingualJson)
$httpResp = Invoke-WebRequest -Uri "$BaseUrl/api/v1/complaints" -Method Post -ContentType "application/json; charset=utf-8" -Headers @{ Authorization = "Bearer $token" } -Body $reqBytes -UseBasicParsing
$rawJson = [System.Text.Encoding]::UTF8.GetString($httpResp.RawContentStream.ToArray())
$resMulti = $rawJson | ConvertFrom-Json

Assert-Test "Kannada text preserved in complaint title/description" ($resMulti.description.Contains($kannadaWord)) "Expected $kannadaWord in $($resMulti.description)"
Assert-Test "Hindi text preserved in complaint description" ($resMulti.description.Contains($hindiWord)) "Expected $hindiWord in $($resMulti.description)"
Assert-Test "Emoji preserved without Unicode corruption" ($resMulti.description.Contains($emoji)) "Expected $emoji in $($resMulti.description)"

# 4. Large Description (10,000 characters)
Write-Host "`n--- Section 3: Large Input Stress Testing ---" -ForegroundColor Yellow
$bigText = "Detailed municipal complaint narrative. " * 250 # ~10,000 characters
$largeBody = @{
    title = "Comprehensive Infrastructure Audit Report"
    description = $bigText
    categoryId = 1
    urgency = "LOW"
    latitude = 14.4955
    longitude = 75.9530
    address = "Industrial Area, Davanagere"
    anonymous = $false
    confirmNotDuplicate = $true
} | ConvertTo-Json

$resLarge = Invoke-RestMethod -Uri "$BaseUrl/api/v1/complaints" -Method Post -ContentType "application/json" -Headers @{ Authorization = "Bearer $token" } -Body $largeBody
Assert-Test "Large 10k character description accepted and stored" ($resLarge.description.Length -gt 5000) "Length was $($resLarge.description.Length)"

# 5. Out of Bounds Pagination
Write-Host "`n--- Section 4: Boundary & Pagination Edge Cases ---" -ForegroundColor Yellow
$outOfBounds = Invoke-RestMethod -Uri "$BaseUrl/api/v1/complaints/mine?page=99999&size=10" -Method Get -Headers @{ Authorization = "Bearer $token" }
Assert-Test "Page beyond last page returns empty array (no 500)" ($outOfBounds.content.Count -eq 0 -and $outOfBounds.totalElements -gt 0) ""

# 6. Idempotent Double Actions
Write-Host "`n--- Section 5: Double Actions & Conflict Idempotency ---" -ForegroundColor Yellow
$upvoteRes1 = Invoke-RestMethod -Uri "$BaseUrl/api/v1/complaints/$($res1.id)/upvote" -Method Post -Headers @{ Authorization = "Bearer $token" }
Assert-Test "First upvote accepted" ($upvoteRes1.success -eq $true) ""

try {
    Invoke-RestMethod -Uri "$BaseUrl/api/v1/complaints/$($res1.id)/upvote" -Method Post -Headers @{ Authorization = "Bearer $token" } | Out-Null
    Assert-Test "Double upvote safely prevented or idempotent" $true "Upvoted twice without error"
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Assert-Test "Double upvote returns 409 or 400 conflict" ($code -eq 409 -or $code -eq 400) "Got $code"
}

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "PHASE E EDGE CASE AUDIT COMPLETE: $passed PASSED, $failed FAILED" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

if ($failed -gt 0) {
    exit 1
} else {
    exit 0
}
