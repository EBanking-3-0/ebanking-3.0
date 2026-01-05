# Payment Testing Script for E-Banking 3.0
# Usage: .\test_payments.ps1

$BaseUrl = "http://localhost:8085/api/payments"
$Timestamp = Get-Date -UFormat "%s"

# 1. Internal Transfer Test (John to Jane)
Write-Host "`n--- Testing Internal Transfer ---" -ForegroundColor Cyan
try {
    $InternalBody = @{
        fromAccountId   = 1
        toAccountNumber = "FR7630006000011234567890101" # Target account for User 2
        amount          = 50.00
        currency        = "EUR"
        description     = "Virement interne test"
        idempotencyKey  = [PSCustomObject]@{guid=[guid]::NewGuid()}.guid.ToString()
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$BaseUrl/internal?userId=1" -Method Post -ContentType "application/json" -Body $InternalBody
    $response | ConvertTo-Json
} catch {
    Write-Host "Error during Internal Transfer:" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $reader.ReadToEnd()
    } else {
        $_.Exception.Message
    }
}

# 2. SEPA Transfer Test
Write-Host "`n--- Testing SEPA Transfer ---" -ForegroundColor Cyan
try {
    $SepaBody = @{
        fromAccountId   = 1
        toIban          = "FR7630006000019876543210101"
        beneficiaryName = "John Doe SEPA"
        amount          = 150.00
        currency        = "EUR"
        description     = "Virement SEPA test"
        idempotencyKey  = [PSCustomObject]@{guid=[guid]::NewGuid()}.guid.ToString()
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$BaseUrl/sepa?userId=1" -Method Post -ContentType "application/json" -Body $SepaBody
    $response | ConvertTo-Json
} catch {
    Write-Host "Error during SEPA Transfer:" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $reader.ReadToEnd()
    } else {
        $_.Exception.Message
    }
}

# 3. Mobile Recharge Test (MA)
Write-Host "`n--- Testing Mobile Recharge (Morocco) ---" -ForegroundColor Cyan
try {
    $MobileBody = @{
        fromAccountId   = 1
        phoneNumber     = "0612345678"
        countryCode     = "IAM" # Maroc Telecom
        amount          = 20.00
        currency        = "MAD"
        description     = "Recharge Mobile test"
        idempotencyKey  = [PSCustomObject]@{guid=[guid]::NewGuid()}.guid.ToString()
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$BaseUrl/mobile-recharge?userId=1" -Method Post -ContentType "application/json" -Body $MobileBody
    $response | ConvertTo-Json
} catch {
    Write-Host "Error during Mobile Recharge:" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $reader.ReadToEnd()
    } else {
        $_.Exception.Message
    }
}

# 4. List User Payments
Write-Host "`n--- Listing Payments for User 1 ---" -ForegroundColor Cyan
Invoke-RestMethod -Uri "$BaseUrl/user?userId=1" -Method Get
