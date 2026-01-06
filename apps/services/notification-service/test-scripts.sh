# Notification Service Test Scripts
# Use these curl commands to test the service manually

# 1. Health Check
echo "=== Testing Health Check ==="
curl -X GET http://localhost:8080/api/notifications/health

# 2. Send Simple Email
echo -e "\n\n=== Testing Simple Email ==="
curl -X POST http://localhost:8080/api/notifications/test/email/simple \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "email": "test@example.com", 
    "subject": "Test Email",
    "content": "This is a test email from notification service"
  }'

# 3. Send Templated Email
echo -e "\n\n=== Testing Templated Email ==="
curl -X POST "http://localhost:8080/api/notifications/test/email/template?userId=1&email=test@example.com&templateCode=welcome-email" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "amount": "$1,000.00",
    "date": "2025-12-19"
  }'

# 4. Send SMS
echo -e "\n\n=== Testing SMS ==="
curl -X POST "http://localhost:8080/api/notifications/test/sms?userId=1&phoneNumber=%2B1234567890&message=Test SMS from notification service"

# 5. Send Complex Notification Request
echo -e "\n\n=== Testing Complex Notification ==="
curl -X POST http://localhost:8080/api/notifications/test/simple \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "recipient": "test@example.com",
    "notificationType": "TRANSACTION",
    "channel": "EMAIL",
    "subject": "Transaction Alert",
    "content": "Your account has been credited with $500.00",
    "templateCode": "transaction-alert",
    "templateData": {
      "amount": "$500.00",
      "account": "****1234",
      "timestamp": "2025-12-19 10:30:00"
    }
  }'

# 6. Get Notification History
echo -e "\n\n=== Testing Notification History ==="
curl -X GET http://localhost:8080/api/notifications/history/1

# 7. Trigger Retry Mechanism
echo -e "\n\n=== Testing Retry Mechanism ==="
curl -X POST http://localhost:8080/api/notifications/test/retry

echo -e "\n\n=== All tests completed ==="