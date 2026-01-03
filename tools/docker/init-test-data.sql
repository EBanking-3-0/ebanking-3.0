-- Test data for E-Banking 3.0
-- This script inserts test users and accounts with IBANs for payment testing

-- Connect to ebanking database
\c ebanking;

-- Set schema
SET search_path TO accounts, public;

-- Insert test accounts with IBANs
-- Note: These will only work if the accounts table already exists (created by Hibernate)

-- User ID 1 - John Doe
INSERT INTO accounts.accounts (account_number, iban, user_id, balance, currency, type, status, created_at, updated_at)
VALUES 
  ('ACC001', 'FR7620041010050500013M02606', 1, 10000.00, 'EUR', 'CHECKING', 'ACTIVE', NOW(), NOW())
ON CONFLICT (account_number) DO NOTHING;

INSERT INTO accounts.accounts (account_number, iban, user_id, balance, currency, type, status, created_at, updated_at)
VALUES 
  ('ACC002', 'FR7620041010050500013M02607', 1, 5000.00, 'EUR', 'SAVINGS', 'ACTIVE', NOW(), NOW())
ON CONFLICT (account_number) DO NOTHING;

-- User ID 2 - Jane Smith
INSERT INTO accounts.accounts (account_number, iban, user_id, balance, currency, type, status, created_at, updated_at)
VALUES 
  ('ACC003', 'FR7620041010050500013M02608', 2, 15000.00, 'EUR', 'CHECKING', 'ACTIVE', NOW(), NOW())
ON CONFLICT (account_number) DO NOTHING;

INSERT INTO accounts.accounts (account_number, iban, user_id, balance, currency, type, status, created_at, updated_at)
VALUES 
  ('ACC004', 'FR7620041010050500013M02609', 2, 8000.00, 'EUR', 'SAVINGS', 'ACTIVE', NOW(), NOW())
ON CONFLICT (account_number) DO NOTHING;

-- User ID 3 - Bob Johnson
INSERT INTO accounts.accounts (account_number, iban, user_id, balance, currency, type, status, created_at, updated_at)
VALUES 
  ('ACC005', 'FR7620041010050500013M02610', 3, 20000.00, 'EUR', 'CHECKING', 'ACTIVE', NOW(), NOW())
ON CONFLICT (account_number) DO NOTHING;

-- Update existing accounts to add IBAN if they exist
UPDATE accounts.accounts 
SET iban = 'FR7620041010050500013M02606'
WHERE account_number = 'ACC001' AND iban IS NULL;

UPDATE accounts.accounts 
SET iban = 'FR7620041010050500013M02607'
WHERE account_number = 'ACC002' AND iban IS NULL;

UPDATE accounts.accounts 
SET iban = 'FR7620041010050500013M02608'
WHERE account_number = 'ACC003' AND iban IS NULL;

UPDATE accounts.accounts 
SET iban = 'FR7620041010050500013M02609'
WHERE account_number = 'ACC004' AND iban IS NULL;

UPDATE accounts.accounts 
SET iban = 'FR7620041010050500013M02610'
WHERE account_number = 'ACC005' AND iban IS NULL;

-- Display inserted accounts
SELECT id, account_number, iban, user_id, balance, currency, type, status 
FROM accounts.accounts 
ORDER BY id;
