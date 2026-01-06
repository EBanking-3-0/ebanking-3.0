# API Reference - User Service

## Base URL

```
http://localhost:8083/api/v1
```

## Authentification

Tous les endpoints requièrent un JWT Keycloak valide dans le header:

```
Authorization: Bearer {jwt_token}
```

Le JWT doit contenir :
- `sub`: keycloakId unique
- `email`: Email de l'utilisateur
- `given_name`: Prénom (optionnel)
- `family_name`: Nom (optionnel)
- `preferred_username`: Username (optionnel)

## Endpoints Utilisateur

### 1. Récupérer le profil de l'utilisateur actuel

**Endpoint:**
```
GET /users/me
```

**Description:**
Récupère le profil complet de l'utilisateur actuellement authentifié.

**Headers:**
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**Réponse (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "keycloakId": "user-123",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+216 50 123 456",
  "addressLine1": "123 Rue de la Paix",
  "addressLine2": "Apt 5",
  "city": "Tunis",
  "postalCode": "1000",
  "country": "Tunisia",
  "status": "ACTIVE",
  "kycStatus": "VERIFIED",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-20T14:45:00"
}
```

**Erreurs:**
- `401 Unauthorized`: JWT invalide ou expiré
- `404 Not Found`: Utilisateur n'existe pas (pas de KYC soumis)

**Exemple cURL:**
```bash
curl -X GET http://localhost:8083/api/v1/users/me \
  -H "Authorization: Bearer {jwt_token}"
```

**Exemple JavaScript:**
```javascript
fetch('http://localhost:8083/api/v1/users/me', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${jwtToken}`
  }
})
.then(response => response.json())
.then(data => console.log(data))
```

---

### 2. Récupérer le profil d'un utilisateur spécifique

**Endpoint:**
```
GET /users/{userId}
```

**Description:**
Récupère le profil d'un utilisateur spécifique par son UUID.
Restreint: Admin ou propriétaire du compte.

**Paramètres:**
| Paramètre | Type | Description |
|-----------|------|-------------|
| userId | UUID | UUID de l'utilisateur |

**Headers:**
```
Authorization: Bearer {jwt_token}
```

**Réponse (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "status": "ACTIVE",
  "kycStatus": "VERIFIED"
}
```

**Erreurs:**
- `401 Unauthorized`: JWT invalide
- `403 Forbidden`: Accès non autorisé
- `404 Not Found`: Utilisateur non trouvé

---

### 3. Supprimer le compte utilisateur

**Endpoint:**
```
DELETE /users/me
```

**Description:**
Supprime le compte de l'utilisateur actuel et toutes les données associées.
**ATTENTION: Opération irréversible!**

**Headers:**
```
Authorization: Bearer {jwt_token}
```

**Réponse (204 No Content):**
```
(Pas de corps)
```

**Erreurs:**
- `401 Unauthorized`: JWT invalide
- `404 Not Found`: Utilisateur non trouvé

---

## Endpoints KYC (Know Your Customer)

### 1. Soumettre une vérification KYC

**Endpoint:**
```
POST /kyc
```

**Description:**
Soumet une vérification KYC pour l'utilisateur actuel.
Crée l'utilisateur à partir du JWT Keycloak s'il n'existe pas.

**Headers:**
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+216 50 123 456",
  "cinNumber": "12345678",
  "addressLine1": "123 Rue de la Paix",
  "addressLine2": "Apt 5",
  "city": "Tunis",
  "postalCode": "1000",
  "country": "Tunisia",
  "cinImageBase64": "data:image/png;base64,iVBORw0KGgoAAAANS...",
  "selfieImageBase64": "data:image/png;base64,iVBORw0KGgoAAAANS...",
  "gdprConsents": {
    "MARKETING": true,
    "DATA_PROCESSING": true,
    "THIRD_PARTY": false
  }
}
```

**Validation:**
```
- firstName: obligatoire, non vide
- lastName: obligatoire, non vide
- phone: obligatoire, format valide
- cinNumber: obligatoire, format CIN valide
- addressLine1: obligatoire, non vide
- city: obligatoire, non vide
- country: obligatoire, non vide
- cinImageBase64: image valide en base64
- selfieImageBase64: image valide en base64
```

**Réponse (201 Created):**
```json
{
  "id": "kyc-123-uuid",
  "user": {
    "id": "user-456-uuid"
  },
  "cinNumber": "12345678",
  "idDocumentUrl": "/storage/user-456-uuid/cin-2024-01-20.png",
  "selfieUrl": "/storage/user-456-uuid/selfie-2024-01-20.png",
  "status": "PENDING_REVIEW",
  "createdAt": "2024-01-20T14:30:00",
  "updatedAt": "2024-01-20T14:30:00"
}
```

**Erreurs:**
- `400 Bad Request`: Validation échouée
- `401 Unauthorized`: JWT invalide
- `409 Conflict`: KYC déjà soumise et en attente
- `500 Internal Server Error`: Erreur lors du traitement

**Exemple cURL:**
```bash
curl -X POST http://localhost:8083/api/v1/kyc \
  -H "Authorization: Bearer {jwt_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+216 50 123 456",
    "cinNumber": "12345678",
    "addressLine1": "123 Rue de la Paix",
    "city": "Tunis",
    "postalCode": "1000",
    "country": "Tunisia",
    "cinImageBase64": "...",
    "selfieImageBase64": "...",
    "gdprConsents": {
      "MARKETING": true,
      "DATA_PROCESSING": true
    }
  }'
```

**Exemple JavaScript:**
```javascript
const kycData = {
  firstName: "John",
  lastName: "Doe",
  phone: "+216 50 123 456",
  cinNumber: "12345678",
  addressLine1: "123 Rue de la Paix",
  city: "Tunis",
  postalCode: "1000",
  country: "Tunisia",
  cinImageBase64: convertToBase64(cinFile),
  selfieImageBase64: convertToBase64(selfieFile),
  gdprConsents: {
    MARKETING: true,
    DATA_PROCESSING: true,
    THIRD_PARTY: false
  }
};

fetch('http://localhost:8083/api/v1/kyc', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${jwtToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(kycData)
})
.then(response => response.json())
.then(data => console.log('KYC soumise:', data))
.catch(error => console.error('Erreur:', error));
```

---

### 2. Récupérer le statut KYC

**Endpoint:**
```
GET /kyc/status
```

**Description:**
Récupère le statut actuel de la vérification KYC de l'utilisateur.

**Headers:**
```
Authorization: Bearer {jwt_token}
```

**Réponse (200 OK):**
```json
{
  "id": "kyc-123-uuid",
  "cinNumber": "12345678",
  "idDocumentUrl": "/storage/user-456-uuid/cin-2024-01-20.png",
  "selfieUrl": "/storage/user-456-uuid/selfie-2024-01-20.png",
  "addressProofUrl": null,
  "status": "PENDING_REVIEW",
  "verifiedAt": null,
  "verifiedBy": null,
  "createdAt": "2024-01-20T14:30:00",
  "updatedAt": "2024-01-20T14:30:00"
}
```

**Statuts possibles:**
- `PENDING_REVIEW`: En attente de révision manuelle
- `VERIFIED`: KYC approuvée et vérifiée
- `REJECTED`: KYC rejetée
- `MORE_INFO_NEEDED`: Informations supplémentaires requises

**Erreurs:**
- `401 Unauthorized`: JWT invalide
- `404 Not Found`: Utilisateur ou KYC non trouvés

**Exemple cURL:**
```bash
curl -X GET http://localhost:8083/api/v1/kyc/status \
  -H "Authorization: Bearer {jwt_token}"
```

---

## Codes d'erreur HTTP

| Code | Signification | Cause |
|------|---------------|-------|
| 200 | OK | Requête réussie |
| 201 | Created | Ressource créée avec succès |
| 204 | No Content | Opération réussie, pas de contenu |
| 400 | Bad Request | Validation échouée ou paramètres invalides |
| 401 | Unauthorized | JWT manquant ou invalide |
| 403 | Forbidden | Accès refusé (permissions insuffisantes) |
| 404 | Not Found | Ressource non trouvée |
| 409 | Conflict | Conflit (ex: KYC déjà soumise) |
| 500 | Internal Server Error | Erreur serveur |

## Formats de données

### Format d'image (Base64)

Les images doivent être encodées en base64 avec le data URI :

```
data:image/png;base64,iVBORw0KGgoAAAANS...
data:image/jpeg;base64,/9j/4AAQSkZJRg...
```

Exemple JavaScript pour convertir :
```javascript
function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = error => reject(error);
  });
}

// Utilisation
const cinFile = document.getElementById('cinInput').files[0];
const base64 = await fileToBase64(cinFile);
// base64 = "data:image/png;base64,iVBORw0KGgoAAAANS..."
```

### Format de date/heure

Toutes les dates utilisent le format ISO 8601 :
```
2024-01-20T14:30:00
```

## Statuts utilisateur

```
PENDING_REVIEW  → En attente d'approbation KYC
ACTIVE          → KYC approuvée, utilisateur actif
REJECTED        → KYC rejetée, pas d'accès
```

## Limites et quotas

- **Taille max des images**: 5MB par image
- **Nombre max de soumissions KYC**: 1 soumission en attente par utilisateur
- **Rate limit**: 100 requêtes/minute par utilisateur

## Webhooks et événements

Quand l'état KYC change, des événements sont publiés sur Kafka :

```
Topic: user.events

Événement: user.kyc.submitted
{
  "userId": "user-456-uuid",
  "keycloakId": "user-123",
  "eventType": "KYC_SUBMITTED",
  "timestamp": "2024-01-20T14:30:00"
}

Événement: user.kyc.verified
{
  "userId": "user-456-uuid",
  "eventType": "KYC_VERIFIED",
  "verifiedBy": "admin-user",
  "timestamp": "2024-01-21T10:15:00"
}
```

## Exemples complets

### Exemple 1: Soumission complète KYC + récupération statut

```javascript
async function submitKYC(jwtToken) {
  // 1. Convertir les images
  const cinFile = document.getElementById('cin').files[0];
  const selfieFile = document.getElementById('selfie').files[0];
  
  const cinBase64 = await fileToBase64(cinFile);
  const selfieBase64 = await fileToBase64(selfieFile);
  
  // 2. Soumettre KYC
  const kycResponse = await fetch('/api/v1/kyc', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${jwtToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      firstName: 'John',
      lastName: 'Doe',
      phone: '+216 50 123 456',
      cinNumber: '12345678',
      addressLine1: '123 Rue de la Paix',
      city: 'Tunis',
      postalCode: '1000',
      country: 'Tunisia',
      cinImageBase64: cinBase64,
      selfieImageBase64: selfieBase64,
      gdprConsents: {
        MARKETING: true,
        DATA_PROCESSING: true,
        THIRD_PARTY: false
      }
    })
  });
  
  if (kycResponse.status === 201) {
    const kyc = await kycResponse.json();
    console.log('KYC soumise:', kyc);
    
    // 3. Récupérer le statut
    const statusResponse = await fetch('/api/v1/kyc/status', {
      headers: {
        'Authorization': `Bearer ${jwtToken}`
      }
    });
    
    const status = await statusResponse.json();
    console.log('Statut KYC:', status.status); // PENDING_REVIEW
  }
}

function fileToBase64(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = () => resolve(reader.result);
    reader.onerror = error => reject(error);
  });
}
```

### Exemple 2: Récupérer le profil utilisateur

```javascript
async function getProfile(jwtToken) {
  const response = await fetch('/api/v1/users/me', {
    headers: {
      'Authorization': `Bearer ${jwtToken}`
    }
  });
  
  if (response.ok) {
    const profile = await response.json();
    console.log('Prénom:', profile.firstName);
    console.log('Statut utilisateur:', profile.status);
    console.log('Statut KYC:', profile.kycStatus);
  } else if (response.status === 404) {
    console.log('Utilisateur n\'existe pas (pas de KYC soumis)');
  }
}
```

## Support et documentation

Pour plus d'informations :
- API Docs: `http://localhost:8083/swagger-ui.html`
- Architecture: Voir `ARCHITECTURE.md`
- Guide développement: Voir `DEVELOPMENT_GUIDE.md`

