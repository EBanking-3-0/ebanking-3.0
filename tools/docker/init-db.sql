-- Initialize databases for E-Banking 3.0 microservices

-- Create Keycloak database
CREATE DATABASE keycloak;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE ebanking TO ebanking;
GRANT ALL PRIVILEGES ON DATABASE keycloak TO ebanking;

-- Connect to ebanking database
\c ebanking;

-- Create schemas for each microservice (database per service pattern)
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS users;
CREATE SCHEMA IF NOT EXISTS accounts;
CREATE SCHEMA IF NOT EXISTS payments;
CREATE SCHEMA IF NOT EXISTS crypto;
CREATE SCHEMA IF NOT EXISTS notifications;
CREATE SCHEMA IF NOT EXISTS analytics;

-- Grant schema permissions
GRANT ALL ON SCHEMA auth TO ebanking;
GRANT ALL ON SCHEMA users TO ebanking;
GRANT ALL ON SCHEMA accounts TO ebanking;
GRANT ALL ON SCHEMA payments TO ebanking;
GRANT ALL ON SCHEMA crypto TO ebanking;
GRANT ALL ON SCHEMA notifications TO ebanking;
GRANT ALL ON SCHEMA analytics TO ebanking;
