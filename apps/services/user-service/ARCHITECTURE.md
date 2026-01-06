# Architecture User Service

## Vue d'ensemble

Le User Service gère le cycle de vie complet des utilisateurs dans le système e-banking, depuis la création jusqu'à la gestion des vérifications KYC (Know Your Customer) et des consentements GDPR.

## Architecture en couches

```
┌─────────────────────────────────────────────────────────┐
│                API Layer (Controllers)                   │
│  - UserController: Gestion des profils utilisateur      │
│  - KycController: Gestion des vérifications KYC          │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│            Mapper Layer (DTO Conversion)                │
│  - UserMapper: User ↔ DTO                               │
│  - UserProfileMapper: User → UserProfileResponse         │
│  - KycMapper: KycVerification → KycResponse             │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│        Application Service Layer (Business Logic)       │
│  - UserService: Orchestration de la logique métier      │
│    - Extraction JWT Keycloak                            │
│    - Gestion du cycle de vie utilisateur                │
│    - Gestion des vérifications KYC                      │
│    - Gestion des consentements GDPR                     │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│           Domain Model Layer (Entités)                  │
│  - User: Utilisateur du système                         │
│  - KycVerification: Vérification KYC                    │
│  - GdprConsent: Consentements GDPR                      │
│  - UserRepository: Accès aux données                    │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│       Infrastructure Layer (Implémentation)            │
│  - FileStorageService: Stockage des documents          │
│  - Database (JPA/Hibernate)                            │
│  - Kafka Events (UserEventProducer)                    │
└─────────────────────────────────────────────────────────┘
```

## Structure des répertoires

```
user-service/
├── src/main/java/com/ebanking/user/
│   ├── UserServiceApplication.java           # Point d'entrée Spring Boot
│   │
│   ├── api/
│   │   ├── controller/                       # Couche API REST
│   │   │   ├── UserController.java           # Endpoints utilisateurs
│   │   │   └── KycController.java            # Endpoints KYC
│   │   │
│   │   └── mapper/                           # Conversion DTO
│   │       ├── UserMapper.java               # User ↔ DTO
│   │       ├── UserProfileMapper.java        # User → Profile
│   │       └── KycMapper.java                # KycVerification → DTO
│   │
│   ├── application/
│   │   ├── service/
│   │   │   └── UserService.java              # Logique métier
│   │   │
│   │   └── exception/
│   │       └── UserNotFoundException.java    # Exceptions métier
│   │
│   ├── domain/
│   │   ├── model/                            # Entités de domaine
│   │   │   ├── User.java                     # Utilisateur
│   │   │   ├── KycVerification.java          # Vérification KYC
│   │   │   └── GdprConsent.java              # Consentements GDPR
│   │   │
│   │   └── repository/
│   │       └── UserRepository.java           # Accès aux données
│   │
│   ├── infrastructure/
│   │   ├── kafka/
│   │   │   └── UserEventProducer.java        # Publication d'événements
│   │   │
│   │   └── storage/
│   │       └── FileStorageService.java       # Gestion des fichiers
│   │
│   └── config/
│       ├── KeycloakAdminConfig.java          # Configuration Keycloak
│       └── WebConfig.java                    # Configuration Web
│
└── src/test/java/...                         # Tests unitaires
```

## Flux de création d'utilisateur

```
1. Utilisateur s'authentifie via Keycloak
   ↓
2. Client envoie JWT + KYC data à POST /api/v1/kyc
   ↓
3. KycController.submitKyc()
   ├─ Extrait keycloakId du JWT
   ├─ Crée User à partir Keycloak (si n'existe pas)
   └─ Appelle UserService.submitKyc()
   ↓
4. UserService.submitKyc()
   ├─ Vérifie qu'une KYC n'est pas déjà soumise
   ├─ Met à jour le profil utilisateur
   ├─ Stocke les documents (images base64)
   ├─ Crée/met à jour KycVerification (status = PENDING_REVIEW)
   ├─ Enregistre les consentements GDPR
   └─ Sauvegarde l'utilisateur (cascade)
   ↓
5. Retour 201 CREATED avec détails KYC
```

## Flux de vérification KYC

```
1. Admin/Système accède à la vérification KYC
   ↓
2. Révision manuelle ou automatisée
   ↓
3. Décision: Approuver ou Rejeter
   ↓
4. Mise à jour du statut KycVerification
   ├─ status = VERIFIED ou REJECTED
   ├─ verifiedAt = maintenant
   └─ verifiedBy = agent qui a vérifié
   ↓
5. Mise à jour du statut User
   ├─ status = ACTIVE (si approuvé)
   └─ status = REJECTED (si rejeté)
   ↓
6. Publication d'événement Kafka (UserVerifiedEvent ou UserRejectedEvent)
```

## Endpoints principaux

### UserController - Gestion des profils

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/v1/users/me` | Profil utilisateur actuel |
| GET | `/api/v1/users/{userId}` | Profil d'un utilisateur spécifique |
| DELETE | `/api/v1/users/me` | Supprimer le compte actuel |
| GET | `/api/v1/users/test` | Endpoint de test |

### KycController - Vérification KYC

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/v1/kyc` | Soumettre une vérification KYC |
| GET | `/api/v1/kyc/status` | Récupérer le statut KYC |

## Statuts utilisateur

```
┌─────────────────────────────────────────────────────────┐
│                    PENDING_REVIEW                       │
│   Création initiale, attente d'approbation KYC          │
│                      ↓                                  │
│    ┌──────────────────┴──────────────────┐             │
│    ↓                                      ↓             │
│  ACTIVE                               REJECTED         │
│  KYC approuvée                    KYC rejetée         │
│  Utilisateur actif                Pas d'accès         │
└─────────────────────────────────────────────────────────┘
```

## Statuts KYC

```
PENDING_REVIEW ──→ VERIFIED ──→ Utilisateur ACTIVE
    ↓
    └──→ REJECTED ──→ Utilisateur REJECTED
    ↓
    └──→ MORE_INFO_NEEDED ──→ Resoumission
```

## Extraction JWT

Le UserService extrait les informations du JWT Keycloak :

```java
// Depuis le JWT:
String keycloakId    = jwt.getClaim("sub")                  // ID unique
String email         = jwt.getClaim("email")                // Email
String firstName     = jwt.getClaim("given_name")           // Prénom
String lastName      = jwt.getClaim("family_name")          // Nom
String username      = jwt.getClaim("preferred_username")   // Username
```

## Gestion des fichiers

Les documents KYC sont stockés en base64 et sauvegardés via `FileStorageService` :

- **CIN Image**: Numérisée de la carte d'identité
- **Selfie**: Photo de l'utilisateur avec la carte d'identité
- **Address Proof** (optionnel): Preuve d'adresse

Chemin de stockage: `{userId}/{type}/{timestamp}.{ext}`

## Consentements GDPR

Les consentements sont enregistrés lors de la soumission KYC :

```java
Map<String, Boolean> consents = {
    "MARKETING": true,           // Consentement marketing
    "DATA_PROCESSING": true,     // Traitement des données
    "THIRD_PARTY": false        // Partage avec tiers
}
```

## Sécurité

- **Authentification**: JWT Keycloak requis pour tous les endpoints
- **Autorisation**: L'utilisateur ne peut accéder qu'à ses propres données
- **Validation**: ValidationDTOs pour les entrées utilisateur
- **Transactions**: @Transactional pour la cohérence des données

## Transactions

Le service utilise les transactions Spring pour garantir la cohérence :

```java
@Transactional
public KycVerification submitKyc(User user, KycRequest kycRequest) {
    // Atomique: Tout se sauvegarde ou tout échoue
    // Rollback automatique en cas d'exception
}

@Transactional(readOnly = true)
public User getUserByKeycloakId(String keycloakId) {
    // Lecture seule, pas d'écriture
}
```

## Intégration Kafka

Le service publie des événements utilisateur sur Kafka :

- `user.created`: Nouvel utilisateur créé
- `user.kyc.submitted`: KYC soumise
- `user.kyc.verified`: KYC approuvée
- `user.kyc.rejected`: KYC rejetée
- `user.activated`: Utilisateur activé
- `user.deactivated`: Utilisateur désactivé

## Configuration

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost/ebanking_user
    username: postgres
    password: password
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

keycloak:
  realm: ebanking
  auth-server-url: http://localhost:8080/auth
  resource: user-service
  credentials:
    secret: ${KEYCLOAK_CLIENT_SECRET}
```

## Tests

Exécuter les tests :

```bash
./gradlew test
```

Les tests couvrent :
- Extraction JWT
- Création utilisateur
- Soumission KYC
- Gestion des consentements GDPR
- Mapping DTO

## Performance

- **Lazy loading**: Chargement à la demande des relations
- **Indexed columns**: keycloakId, email pour recherches rapides
- **Read-only transactions**: Optimisé pour les lectures
- **Pagination**: Implémentée pour les listes (à venir)

## Maintenance

### Logs importants

```
[USER_SERVICE] - User created: {keycloakId}
[USER_SERVICE] - KYC submitted: {userId}
[USER_SERVICE] - KYC status changed: {userId} -> {newStatus}
```

### Monitoring

Métriques Micrometer à surveiller :

- Nombre d'utilisateurs créés
- Taux de soumission KYC
- Taux d'approbation KYC
- Temps de traitement KYC

