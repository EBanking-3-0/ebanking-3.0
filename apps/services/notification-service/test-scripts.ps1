# Notification Service Test Scripts - PowerShell Version
# Run these commands in PowerShell to test the service

$baseUrl = "http://localhost:8080/api/notifications"

Write-Host "=== Testing Health Check ===" -ForegroundColor Green
Invoke-RestMethod -Uri "$baseUrl/health" -Method Get

Write-Host "`n=== Testing Simple Email ===" -ForegroundColor Green
$emailRequest = @{
    userId = 1
    email = "test@example.com"
    subject = "Test Email"
    content = "This is a test email from notification service"
} | ConvertTo-Json

Invoke-RestMethod -Uri "$baseUrl/test/email/simple" -Method Post -Body $emailRequest -ContentType "application/json"

Write-Host "`n=== Testing Templated Email ===" -ForegroundColor Green
$templateData = @{
    name = "John Doe"
    amount = "`$1,000.00"
    date = "2025-12-19"
} | ConvertTo-Json

$params = "userId=1&email=test@example.com&templateCode=welcome-email"
Invoke-RestMethod -Uri "$baseUrl/test/email/template?$params" -Method Post -Body $templateData -ContentType "application/json"

Write-Host "`n=== Testing SMS ===" -ForegroundColor Green
$smsParams = "userId=1&phoneNumber=%2B1234567890&message=Test SMS from notification service"
Invoke-RestMethod -Uri "$baseUrl/test/sms?$smsParams" -Method Post

Write-Host "`n=== Testing Complex Notification ===" -ForegroundColor Green
$complexRequest = @{
    userId = 1
    recipient = "test@example.com"
    notificationType = "TRANSACTION"
    channel = "EMAIL"
    subject = "Transaction Alert"
    content = "Your account has been credited with `$500.00"
    templateCode = "transaction-alert"
    templateData = @{
        amount = "`$500.00"
        account = "****1234"
        timestamp = "2025-12-19 10:30:00"
    }
} | ConvertTo-Json -Depth 3

Invoke-RestMethod -Uri "$baseUrl/test/simple" -Method Post -Body $complexRequest -ContentType "application/json"

Write-Host "`n=== Testing Notification History ===" -ForegroundColor Green
Invoke-RestMethod -Uri "$baseUrl/history/1" -Method Get

Write-Host "`n=== Testing Retry Mechanism ===" -ForegroundColor Green
Invoke-RestMethod -Uri "$baseUrl/test/retry" -Method Post

Write-Host "`n=== All tests completed ===" -ForegroundColor Green