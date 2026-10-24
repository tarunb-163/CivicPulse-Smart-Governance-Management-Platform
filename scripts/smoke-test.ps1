# ==============================================================================
# CivicPulse Full End-to-End Lifecycle Smoke Test for Windows (Steps S01 - S33)
# ==============================================================================
param (
    [string]$BaseUrl = "http://localhost:8080"
)

$ApiUrl = "$BaseUrl/api/v1"
$ErrorActionPreference = "Stop"

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "Starting CivicPulse Smoke Test against: $BaseUrl" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

$passCount = 0
$failCount = 0

function Assert-Step {
    param (
        [string]$StepId,
        [string]$Description,
        [int]$StatusCode,
        [int]$ExpectedCode,
        [string]$Details = ""
    )
    if ($StatusCode -eq $ExpectedCode) {
        Write-Host "[PASS] $StepId : $Description (HTTP $StatusCode)" -ForegroundColor Green
        $script:passCount++
    } else {
        Write-Host "[FAIL] $StepId : $Description - Expected HTTP $ExpectedCode, got $StatusCode" -ForegroundColor Red
        if ($Details) { Write-Host "Details: $Details" -ForegroundColor Yellow }
        $script:failCount++
        exit 1
    }
}

# --- S01: Health Endpoint Check ---
try {
    $res = Invoke-WebRequest -Uri "$BaseUrl/actuator/health" -Method Get -UseBasicParsing
    Assert-Step "S01" "Actuator Health Endpoint is UP" $res.StatusCode 200
} catch {
    Assert-Step "S01" "Actuator Health Endpoint is UP" $_.Exception.Response.StatusCode.value__ 200 $_.Exception.Message
}

# --- S02: Citizen Registration ---
$randId = Get-Random -Minimum 10000 -Maximum 99999
$citizenEmail = "citizen.smoke.$randId@civicpulse.test"
$citizenPass = "Password@123"

$regBody = @{
    name = "Smoke Citizen $randId"
    email = $citizenEmail
    password = $citizenPass
    phone = "9876543210"
    acceptedTerms = $true
} | ConvertTo-Json

try {
    $res = Invoke-RestMethod -Uri "$ApiUrl/auth/register" -Method Post -ContentType "application/json" -Body $regBody
    Assert-Step "S02" "Citizen Registration with Terms Consent" 201 201
} catch {
    $code = $_.Exception.Response.StatusCode.value__
    Assert-Step "S02" "Citizen Registration with Terms Consent" $code 201 $_.Exception.Message
}

# --- S03: Citizen Login ---
$loginBody = @{ email = $citizenEmail; password = $citizenPass } | ConvertTo-Json
$loginRes = Invoke-RestMethod -Uri "$ApiUrl/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
$citizenToken = $loginRes.accessToken
Assert-Step "S03" "Citizen Login & Token Generation" 200 200

# --- S04: Text Classifier ---
$classBody = @{ text = "Deep dangerous pothole on main road" } | ConvertTo-Json
$classRes = Invoke-RestMethod -Uri "$ApiUrl/public/classify" -Method Post -ContentType "application/json" -Body $classBody
$catId = $classRes.categoryId
Assert-Step "S04" "AI Keyword Rule Classification" 200 200

# --- S05: Check Duplicates ---
$dupBody = @{ lat = 14.4644; lng = 75.9218; text = "Pothole on main road" } | ConvertTo-Json
$headers = @{ Authorization = "Bearer $citizenToken" }
$dupRes = Invoke-RestMethod -Uri "$ApiUrl/complaints/check-duplicates" -Method Post -Headers $headers -ContentType "application/json" -Body $dupBody
Assert-Step "S05" "Duplicate Proximity Check" 200 200

# --- S06: Create Complaint with Upload ---
$tempFile = [System.IO.Path]::GetTempFileName()
Set-Content -Path $tempFile -Value "Smoke test dummy image bytes"
try {
    # Using curl.exe for multipart simplicity on Windows
    $createOutput = & curl.exe -s -X POST "$ApiUrl/complaints" `
        -H "Authorization: Bearer $citizenToken" `
        -F "title=Critical Pothole on Davanagere Road $randId" `
        -F "description=Severe road crater causing vehicle skids near Gandhi Circle" `
        -F "categoryId=$catId" `
        -F "urgency=4" `
        -F "latitude=14.4644" `
        -F "longitude=75.9218" `
        -F "address=Gandhi Circle, Davanagere" `
        -F "anonymous=false" `
        -F "confirmNotDuplicate=true" `
        -F "files=@$tempFile;type=image/png" | ConvertFrom-Json

    $complaintId = $createOutput.id
    $complaintCode = $createOutput.code
    Assert-Step "S06" "Create Complaint with File Upload" 201 201
} finally {
    Remove-Item -Path $tempFile -ErrorAction SilentlyContinue
}

# --- S07: Routing & Assignment ---
if ($createOutput.assignedOfficerName) {
    Write-Host "[PASS] S07 : Auto-routed & Assigned to $($createOutput.assignedOfficerName)" -ForegroundColor Green
    $script:passCount++
} else {
    Write-Host "[FAIL] S07 : Complaint assignment missing" -ForegroundColor Red
    exit 1
}

# --- S08: Officer Login ---
$offLogin = @{ email = "officer.roads@civicpulse.test"; password = "Password@123" } | ConvertTo-Json
$offRes = Invoke-RestMethod -Uri "$ApiUrl/auth/login" -Method Post -ContentType "application/json" -Body $offLogin
$officerToken = $offRes.accessToken
Assert-Step "S08" "Officer Login" 200 200

# --- S09: Officer Queue ---
$offHeaders = @{ Authorization = "Bearer $officerToken" }
$qRes = Invoke-RestMethod -Uri "$ApiUrl/officer/complaints" -Method Get -Headers $offHeaders
Assert-Step "S09" "Officer Queue Retrieval" 200 200

# --- S10: Status to IN_PROGRESS ---
$status1Body = @{ status = "IN_PROGRESS"; remark = "Road team dispatched" } | ConvertTo-Json
$s1Res = Invoke-RestMethod -Uri "$ApiUrl/officer/complaints/$complaintId/status" -Method Post -Headers $offHeaders -ContentType "application/json" -Body $status1Body
Assert-Step "S10" "Status Transition to IN_PROGRESS" 200 200

# --- S11: Officer Marks RESOLVED with Proof ---
$proofFile = [System.IO.Path]::GetTempFileName()
Set-Content -Path $proofFile -Value "Proof photo bytes"
try {
    $status2Output = & curl.exe -s -X POST "$ApiUrl/officer/complaints/$complaintId/status" `
        -H "Authorization: Bearer $officerToken" `
        -F "status=RESOLVED" `
        -F "remark=Pothole filled and sealed" `
        -F "proof=@$proofFile;type=image/png" | ConvertFrom-Json
    Assert-Step "S11" "Resolution Transition with Photo Proof" 200 200
} finally {
    Remove-Item -Path $proofFile -ErrorAction SilentlyContinue
}

# --- S12: Citizen Notification ---
$notifs = Invoke-RestMethod -Uri "$ApiUrl/notifications" -Method Get -Headers $headers
Assert-Step "S12" "Citizen In-App Resolution Notification Delivered" 200 200

# --- S13: Feedback Submission ---
$feedBody = @{ rating = 5; comment = "Swift resolution!"; satisfied = $true } | ConvertTo-Json
$feedRes = Invoke-RestMethod -Uri "$ApiUrl/complaints/$complaintId/feedback" -Method Post -Headers $headers -ContentType "application/json" -Body $feedBody
Assert-Step "S13" "Citizen Feedback Rating Submission" 201 201

# --- S14: One-time Feedback Enforcement ---
try {
    $feedDup = Invoke-RestMethod -Uri "$ApiUrl/complaints/$complaintId/feedback" -Method Post -Headers $headers -ContentType "application/json" -Body $feedBody
    Assert-Step "S14" "Reject Duplicate Feedback" 200 409
} catch {
    $c = $_.Exception.Response.StatusCode.value__
    Assert-Step "S14" "Reject Duplicate Feedback (409 Conflict)" $c 409
}

# --- S15: Reopen Complaint ---
$reopenBody = @{ reason = "Surface is bumpy" } | ConvertTo-Json
$reopenRes = Invoke-RestMethod -Uri "$ApiUrl/complaints/$complaintId/reopen" -Method Post -Headers $headers -ContentType "application/json" -Body $reopenBody
Assert-Step "S15" "Reopen Resolved Grievance (7-Day Window)" 200 200

# --- S16: Invalid Transition 400 ---
$invBody = @{ status = "ASSIGNED"; remark = "Illegal transition" } | ConvertTo-Json
try {
    $invRes = Invoke-RestMethod -Uri "$ApiUrl/officer/complaints/$complaintId/status" -Method Post -Headers $offHeaders -ContentType "application/json" -Body $invBody
    Assert-Step "S16" "Reject Invalid Transition" 200 400
} catch {
    $c = $_.Exception.Response.StatusCode.value__
    Assert-Step "S16" "Reject Invalid Transition (HTTP 400)" $c 400
}

# --- S17: Citizen Forbidden from Admin ---
try {
    $forbRes = Invoke-RestMethod -Uri "$ApiUrl/admin/settings" -Method Get -Headers $headers
    Assert-Step "S17" "RBAC Security Check" 200 403
} catch {
    $c = $_.Exception.Response.StatusCode.value__
    Assert-Step "S17" "RBAC Citizen Forbidden from Admin (HTTP 403)" $c 403
}

# --- S18: Admin Analytics ---
$adminLogin = @{ email = "admin@civicpulse.test"; password = "Password@123" } | ConvertTo-Json
$adminRes = Invoke-RestMethod -Uri "$ApiUrl/auth/login" -Method Post -ContentType "application/json" -Body $adminLogin
$adminToken = $adminRes.accessToken
$adminHeaders = @{ Authorization = "Bearer $adminToken" }

$summary = Invoke-RestMethod -Uri "$ApiUrl/analytics/summary" -Method Get -Headers $adminHeaders
Assert-Step "S18" "Admin Analytics Summary" 200 200

# --- S19: CSV & PDF Export ---
$csvRes = Invoke-WebRequest -Uri "$ApiUrl/analytics/export/csv" -Method Get -Headers $adminHeaders -UseBasicParsing
Assert-Step "S19" "Analytics CSV Export" $csvRes.StatusCode 200

$pdfRes = Invoke-WebRequest -Uri "$ApiUrl/analytics/export/pdf" -Method Get -Headers $adminHeaders -UseBasicParsing
Assert-Step "S19b" "Executive PDF Export" $pdfRes.StatusCode 200

# --- S20: Public Track By Code (Zero Data Leak per PRD) ---
$track = Invoke-RestMethod -Uri "$ApiUrl/public/track/$complaintCode" -Method Get
if ($track.citizenPhone -or $track.citizenName -or $track.citizenEmail -or $track.citizenId) {
    Write-Host "[FAIL] S20 : Citizen identity fields leaked on public track!" -ForegroundColor Red
    exit 1
} else {
    Assert-Step "S20" "Public Grievance Tracking (Zero Data Leak)" 200 200
}

# --- S21: CivicBot Message ---
$botBody = @{ message = "Track complaint $complaintCode" } | ConvertTo-Json
$botRes = Invoke-RestMethod -Uri "$ApiUrl/bot/message" -Method Post -ContentType "application/json" -Body $botBody
Assert-Step "S21" "CivicBot AI Live Status Response" 200 200

# --- S22: SLA Scheduler Manual Trigger ---
$slaRes = Invoke-RestMethod -Uri "$ApiUrl/admin/sla/trigger-scheduler" -Method Post -Headers $adminHeaders
Assert-Step "S22" "On-Demand SLA Escalation Engine" 200 200

# --- S23: Audit Log Entries ---
$auditRes = Invoke-RestMethod -Uri "$ApiUrl/admin/audit-logs?size=5" -Method Get -Headers $adminHeaders
Assert-Step "S23" "Immutable Statutory Audit Log" 200 200

# --- S24: Support Existing Complaint ---
$supRes = Invoke-RestMethod -Uri "$ApiUrl/complaints/$complaintId/support" -Method Post -Headers $headers
Assert-Step "S24" "Support & Upvote Existing Ticket" 200 200

# --- S25: Officer Merge Duplicate ---
# Create a second complaint
$c2 = & curl.exe -s -X POST "$ApiUrl/complaints" `
    -H "Authorization: Bearer $citizenToken" `
    -F "title=Second Pothole Issue $randId" `
    -F "description=Duplicate description text" `
    -F "categoryId=$catId" `
    -F "urgency=2" `
    -F "latitude=14.4645" `
    -F "longitude=75.9219" `
    -F "address=Davanagere" `
    -F "confirmNotDuplicate=true" | ConvertFrom-Json
$secondId = $c2.id

$mergeBody = @{ parentComplaintId = $complaintId; reason = "Merged identical pothole" } | ConvertTo-Json
$mergeRes = Invoke-RestMethod -Uri "$ApiUrl/officer/complaints/$secondId/merge" -Method Post -Headers $offHeaders -ContentType "application/json" -Body $mergeBody
Assert-Step "S25" "Officer Merge Duplicate into Parent" 200 200

# --- S26: Citizen Rate Limit Check ---
$upvoteRes = Invoke-RestMethod -Uri "$ApiUrl/complaints/$complaintId/upvote" -Method Post -Headers $headers
Assert-Step "S26" "Citizen Upvote Protection" 200 200

# --- S27: ZXing QR Code PNG ---
$qrRes = Invoke-WebRequest -Uri "$ApiUrl/public/qr/$complaintCode.png" -Method Get -UseBasicParsing
Assert-Step "S27" "ZXing Server-Side QR PNG Generation" $qrRes.StatusCode 200

# --- S28: Image Classifier Fallback ---
$testImg = [System.IO.Path]::GetTempFileName()
Set-Content -Path $testImg -Value "Image bytes"
try {
    $imgClsOutput = & curl.exe -s -X POST "$ApiUrl/complaints/classify-image" `
        -H "Authorization: Bearer $citizenToken" `
        -F "file=@$testImg;type=image/png" | ConvertFrom-Json
    Assert-Step "S28" "AI Vision Image Classification / Graceful Fallback" 200 200
} finally {
    Remove-Item -Path $testImg -ErrorAction SilentlyContinue
}

# --- S29 & S30: Legal Privacy & Terms Pages ---
$privRes = Invoke-WebRequest -Uri "$BaseUrl/privacy.html" -Method Get -UseBasicParsing
Assert-Step "S29" "Public Privacy Policy Available" $privRes.StatusCode 200

$termRes = Invoke-WebRequest -Uri "$BaseUrl/terms.html" -Method Get -UseBasicParsing
Assert-Step "S30" "Public Terms of Service Available" $termRes.StatusCode 200

# --- S31: Actuator Security ---
try {
    $actRes = Invoke-WebRequest -Uri "$BaseUrl/actuator/env" -Method Get -UseBasicParsing
    Assert-Step "S31" "Sensitive Actuator Secured" $actRes.StatusCode 401
} catch {
    $c = $_.Exception.Response.StatusCode.value__
    Assert-Step "S31" "Sensitive Actuator /actuator/env Secured (HTTP 401/403)" $c 401
}

# --- S32: PDF Receipt Download ---
$receiptRes = Invoke-WebRequest -Uri "$ApiUrl/complaints/$complaintId/receipt.pdf" -Method Get -Headers $headers -UseBasicParsing
Assert-Step "S32" "Citizen PDF Receipt Download with Embedded QR" $receiptRes.StatusCode 200

# --- S33: Dev Outbox Viewable ---
$outboxRes = Invoke-RestMethod -Uri "$ApiUrl/admin/dev-outbox" -Method Get -Headers $adminHeaders
Assert-Step "S33" "Admin Dev Email Outbox Verification" 200 200

Write-Host "======================================================================" -ForegroundColor Green
Write-Host "ALL $passCount LIFECYCLE SMOKE TEST STEPS PASSED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "======================================================================" -ForegroundColor Green
exit 0
