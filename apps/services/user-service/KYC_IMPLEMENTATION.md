# User Service - KYC Implementation Documentation

## Overview

This document explains the KYC (Know Your Customer) implementation in the User Service, including user profile management, identity verification, GDPR consent handling, and file storage.

## Table of Contents

1. [Architecture](#architecture)
2. [Components](#components)
3. [API Endpoints](#api-endpoints)
4. [KYC Flow](#kyc-flow)
5. [GDPR Consent Management](#gdpr-consent-management)
6. [File Storage](#file-storage)
7. [Database Schema](#database-schema)
8. [Integration Guide](#integration-guide)
9. [Configuration](#configuration)

## Architecture

The User Service is built with Spring Boot and follows a layered architecture:

```
┌─────────────────────────────────────────┐
│         API Layer (Controllers)         │
│  - KycController                         │
│  - UserController                        │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│      Application Layer (Services)        │
│  - UserService                           │
│  - FileStorageService                    │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│        Domain Layer (Models)              │
│  - User                                  │
│  - KycVerification                       │
│  - GdprConsent                           │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│    Infrastructure Layer (Repositories)   │
│  - UserRepository                        │
└─────────────────────────────────────────┘
```

## Components

### 1. Domain Models

#### User Entity

- **Purpose**: Represents a user in the system
- **Key Fields**:
  - `keycloakId`: Unique identifier from Keycloak (JWT "sub" claim)
  - `email`: User's email address
  - `firstName`, `lastName`: Personal information
  - `phone`: Contact number
  - `addressLine1`, `addressLine2`, `city`, `postalCode`, `country`: Address information
  - `status`: User status (PENDING_REVIEW, ACTIVE, REJECTED)
  - `kycVerification`: One-to-one relationship with KYC verification
  - `consents`: One-to-many relationship with GDPR consents

#### KycVerification Entity

- **Purpose**: Stores KYC verification data and status
- **Key Fields**:
  - `cinNumber`: National ID number
  - `idDocumentUrl`: Path to uploaded CIN document image
  - `selfieUrl`: Path to uploaded selfie image
  - `status`: KYC status (PENDING_REVIEW, VERIFIED, REJECTED, MORE_INFO_NEEDED)
  - `verifiedAt`: Timestamp when verification was completed
  - `verifiedBy`: ID of the agent who verified

#### GdprConsent Entity

- **Purpose**: Tracks user's GDPR consent preferences
- **Key Fields**:
  - `consentType`: Type of consent (MARKETING_EMAIL, MARKETING_SMS, etc.)
  - `granted`: Boolean indicating if consent is granted
  - `grantedAt`: Timestamp when consent was granted
  - `revokedAt`: Timestamp when consent was revoked
  - `consentVersion`: Version of the consent policy

### 2. Services

#### UserService

Main service handling user and KYC operations:

**Key Methods**:

- `getKeycloakIdFromJwt(Authentication)`: Extracts Keycloak ID from JWT token
- `getEmailFromJwt(Authentication)`: Extracts email from JWT token
- `getUserByKeycloakId(String)`: Finds user by Keycloak ID
- `getOrCreateUser(String, String)`: Gets existing user or creates new one
- `updateUserProfile(User, KycRequest)`: Updates user profile information
- `submitKyc(User, KycRequest)`: Submits KYC verification with documents
- `updateGdprConsents(User, Map<String, Boolean>)`: Updates GDPR consents
- `getKycVerification(User)`: Gets current KYC status

#### FileStorageService

Handles file uploads and storage:

**Key Methods**:

- `storeBase64Image(String, String, String)`: Stores base64 encoded image
- `storeFile(MultipartFile, String, String)`: Stores multipart file
- `deleteFile(String)`: Deletes a stored file

**Storage Structure**:

```
uploads/
  └── {userId}/
      ├── cin/
      │   └── {uuid}.{ext}
      └── selfie/
          └── {uuid}.{ext}
```

### 3. Controllers

#### KycController

Handles KYC-related endpoints:

- `GET /api/v1/users/me`: Get current user's profile
- `POST /api/v1/users/me/kyc`: Submit KYC verification
- `GET /api/v1/users/me/kyc`: Get current KYC status

## API Endpoints

### Get Current User Profile

```
GET /api/v1/users/me
Authorization: Bearer {JWT_TOKEN}
```

**Response**:

```json
{
  "id": "uuid",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "addressLine1": "123 Main St",
  "city": "New York",
  "postalCode": "10001",
  "country": "USA",
  "status": "PENDING_REVIEW",
  "kycStatus": "PENDING_REVIEW",
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

### Submit KYC Verification

```
POST /api/v1/users/me/kyc
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**Request Body**:

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "addressLine1": "123 Main St",
  "addressLine2": "Apt 4B",
  "city": "New York",
  "postalCode": "10001",
  "country": "USA",
  "cinNumber": "12345678",
  "cinImageBase64": "data:image/jpeg;base64,/9j/4AAQ...",
  "selfieImageBase64": "data:image/jpeg;base64,/9j/4AAQ...",
  "gdprConsents": {
    "MARKETING_EMAIL": true,
    "MARKETING_SMS": false,
    "PERSONALIZED_OFFERS": true
  }
}
```

**Response** (201 Created):

```json
{
  "cinNumber": "12345678",
  "idDocumentUrl": "/uploads/{userId}/cin/{uuid}.jpg",
  "selfieUrl": "/uploads/{userId}/selfie/{uuid}.jpg",
  "status": "PENDING_REVIEW",
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

### Get KYC Status

```
GET /api/v1/users/me/kyc
Authorization: Bearer {JWT_TOKEN}
```

**Response**:

```json
{
  "cinNumber": "12345678",
  "idDocumentUrl": "/uploads/{userId}/cin/{uuid}.jpg",
  "selfieUrl": "/uploads/{userId}/selfie/{uuid}.jpg",
  "status": "PENDING_REVIEW",
  "verifiedAt": null,
  "verifiedBy": null,
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

## KYC Flow

### 1. User Registration & Login

- User registers in Keycloak
- Email verification is completed
- User logs in and receives JWT token

### 2. Initial Access

- User is redirected to KYC page after login
- Frontend calls `GET /api/v1/users/me` to check profile status
- If no profile exists, user is prompted to complete KYC

### 3. KYC Submission

- User fills out KYC form with:
  - Personal information (name, phone, address)
  - CIN number
  - CIN document image (base64 encoded)
  - Selfie image (base64 encoded)
  - GDPR consent preferences
- Frontend calls `POST /api/v1/users/me/kyc`
- Backend:
  1. Creates or updates user profile
  2. Stores uploaded images
  3. Creates/updates KYC verification record with PENDING_REVIEW status
  4. Saves GDPR consents
  5. Returns KYC response

### 4. Verification Process

- External agent reviews submitted documents
- Agent updates KYC status to VERIFIED or REJECTED
- If verified, user status is updated to ACTIVE
- User can now access protected routes

### 5. Status Flow

```
User Registration
    ↓
PENDING_REVIEW (KYC submitted)
    ↓
VERIFIED (Agent approved) → ACTIVE (User can use app)
    OR
REJECTED (Agent rejected) → User must resubmit
```

## GDPR Consent Management

### Consent Types

The system supports the following GDPR consent types:

1. **MARKETING_EMAIL**: Receive promotional emails and newsletters
2. **MARKETING_SMS**: Receive promotional text messages
3. **MARKETING_PHONE**: Receive promotional phone calls
4. **PERSONALIZED_OFFERS**: Receive personalized product and service offers
5. **DATA_SHARING_PARTNERS**: Allow sharing data with trusted partners
6. **ANALYTICS_IMPROVEMENT**: Help improve services through analytics
7. **OPEN_BANKING_SHARING**: Share financial data for open banking services

### Consent Storage

- Consents are stored as separate `GdprConsent` entities
- Only granted consents (true) are persisted
- Each consent includes:
  - Consent type
  - Grant timestamp
  - Consent version (for policy updates)
  - Revocation timestamp (if revoked)

### Consent Updates

- Consents can be updated during KYC submission
- Existing consents are cleared and recreated based on new selections
- This ensures consent history is maintained

## File Storage

### Storage Mechanism

Files are stored locally in the `uploads/` directory (configurable via `app.file-storage.path`).

### File Organization

```
uploads/
  └── {userId}/
      ├── cin/
      │   └── {uuid}.{extension}
      └── selfie/
          └── {uuid}.{extension}
```

### File Access

Files are served via Spring MVC resource handler at `/uploads/**` path.

### Supported Formats

- Images: JPEG, PNG
- Maximum file size: 10MB (configurable)

### Security Considerations

- Files are stored with UUID-based names to prevent enumeration
- Access is controlled through authentication
- Files are associated with specific users

## Database Schema

### Users Table

```sql
CREATE TABLE users.users (
    id UUID PRIMARY KEY,
    keycloak_id VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(255),
    postal_code VARCHAR(255),
    country VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_REVIEW',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### KYC Verifications Table

```sql
CREATE TABLE users.kyc_verifications (
    id UUID PRIMARY KEY,
    user_id UUID UNIQUE NOT NULL REFERENCES users.users(id),
    cin_number VARCHAR(255) NOT NULL,
    id_document_url VARCHAR(500),
    address_proof_url VARCHAR(500),
    selfie_url VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_REVIEW',
    verified_at TIMESTAMP,
    verified_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### GDPR Consents Table

```sql
CREATE TABLE users.gdpr_consents (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users.users(id),
    consent_type VARCHAR(100) NOT NULL,
    granted BOOLEAN NOT NULL DEFAULT false,
    granted_at TIMESTAMP,
    revoked_at TIMESTAMP,
    consent_version VARCHAR(50) DEFAULT 'v1.0',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

## Integration Guide

### Frontend Integration

The frontend Angular application integrates with the User Service through:

1. **User Service** (`user.service.ts`):
   - `getCurrentUserProfile()`: Fetches user profile
   - `submitKyc(kycRequest)`: Submits KYC verification
   - `getKycStatus()`: Gets current KYC status

2. **KYC Component**:
   - Form with validation
   - File upload with preview
   - GDPR consent checkboxes
   - Error handling

3. **Route Guards**:
   - `kycGuard`: Protects KYC page
   - `kycCompleteGuard`: Ensures KYC is completed before accessing protected routes

### Authentication

All endpoints require JWT authentication:

- Token is extracted from `Authorization: Bearer {token}` header
- Keycloak ID is extracted from JWT "sub" claim
- Email is extracted from JWT "email" claim

### Error Handling

**Common Errors**:

- `409 Conflict`: KYC already submitted and pending
- `404 Not Found`: User or KYC not found
- `401 Unauthorized`: Invalid or missing JWT token
- `500 Internal Server Error`: Server-side error

## Configuration

### Application Properties

```yaml
server:
  port: 8083

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: http://localhost:8092/realms/ebanking-realm/protocol/openid-connect/certs
  datasource:
    url: jdbc:postgresql://localhost:5432/ebanking
    username: ebanking
    password: ebanking123
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        default_schema: users

app:
  file-storage:
    path: ./uploads
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

### Keycloak Configuration

- **Realm**: `ebanking-realm`
- **Auth Server URL**: `http://localhost:8092`
- **JWT Claims Used**:
  - `sub`: Keycloak user ID
  - `email`: User email address

## Testing

### Manual Testing Flow

1. **Start Services**:

   ```bash
   # Start PostgreSQL
   # Start Keycloak
   # Start User Service
   ```

2. **Register User in Keycloak**:
   - Navigate to Keycloak admin console
   - Create user in `ebanking-realm`
   - Set email and password
   - Verify email

3. **Test KYC Submission**:
   - Login through frontend
   - Complete KYC form
   - Submit and verify status is PENDING_REVIEW

4. **Verify Data**:
   - Check database for user record
   - Check KYC verification record
   - Check GDPR consents
   - Verify files are stored

## Future Enhancements

1. **Cloud Storage**: Migrate file storage to S3 or similar
2. **Document Validation**: Add automatic document validation
3. **Biometric Verification**: Add face matching between selfie and ID
4. **Consent Management API**: Separate endpoints for consent management
5. **KYC Status Updates**: Webhook/event for KYC status changes
6. **Multi-language Support**: Support for multiple languages in forms
7. **Audit Logging**: Comprehensive audit trail for all operations

## Troubleshooting

### Common Issues

1. **File Upload Fails**:
   - Check `app.file-storage.path` exists and is writable
   - Verify file size is within limits
   - Check base64 encoding is correct

2. **User Not Found**:
   - Verify Keycloak ID matches JWT "sub" claim
   - Check user exists in database
   - Verify JWT token is valid

3. **KYC Already Submitted**:
   - Check existing KYC status
   - Wait for agent review or contact support

4. **Database Connection Issues**:
   - Verify PostgreSQL is running
   - Check connection string in `application.yml`
   - Verify schema exists

## Support

For issues or questions:

- Check logs in `logs/` directory
- Review application logs for detailed error messages
- Contact the development team

---

**Last Updated**: 2024-12-30
**Version**: 1.0.0
