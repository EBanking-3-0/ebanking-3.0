-- Test data for notification service
-- Insert this into your test database

-- Insert test notification templates
INSERT INTO notifications.notification_templates (template_code, template_name, template_file, body, language, created_at, updated_at) VALUES 
('welcome-email', 'Welcome Email Template', 'welcome-email', 'Welcome {{name}} to our banking platform!', 'en', NOW(), NOW()),
('transaction-alert', 'Transaction Alert Template', 'transaction-alert', 'Transaction of {{amount}} was processed for account {{account}}', 'en', NOW(), NOW()),
('password-reset', 'Password Reset Template', 'password-reset', 'Click here to reset your password: {{resetLink}}', 'en', NOW(), NOW());

-- Insert test notification preferences
INSERT INTO notifications.notification_preferences (user_id, notification_type, email_enabled, sms_enabled, push_enabled, in_app_enabled, created_at, updated_at) VALUES
(1, 'WELCOME', true, true, true, true, NOW(), NOW()),
(1, 'TRANSACTION', true, true, false, true, NOW(), NOW()),
(2, 'WELCOME', true, false, true, true, NOW(), NOW()),
(2, 'FRAUD_ALERT', true, true, true, true, NOW(), NOW());

-- Insert some test notifications for history
INSERT INTO notifications.notifications (user_id, recipient, notification_type, channel, subject, content, status, created_at, updated_at) VALUES
(1, 'test1@example.com', 'WELCOME', 'EMAIL', 'Welcome to Banking', 'Welcome to our platform!', 'SENT', NOW() - INTERVAL '2 hours', NOW()),
(1, '+1234567890', 'TRANSACTION', 'SMS', NULL, 'Transaction alert: $100 transferred', 'SENT', NOW() - INTERVAL '1 hour', NOW()),
(2, 'test2@example.com', 'FRAUD_ALERT', 'EMAIL', 'Security Alert', 'Suspicious activity detected', 'FAILED', NOW() - INTERVAL '30 minutes', NOW()),
(2, 'test2@example.com', 'PASSWORD_RESET', 'EMAIL', 'Password Reset', 'Reset your password', 'PENDING', NOW() - INTERVAL '10 minutes', NOW());