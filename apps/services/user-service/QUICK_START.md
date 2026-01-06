# Guide de Démarrage Rapide - User Service

Bienvenue dans le User Service de l'e-banking! Ce guide vous aide à démarrer rapidement.

## 🚀 Démarrage en 5 minutes

### 1. Comprendre l'architecture (3 min)

```
Lire: ARCHITECTURE.md - Section "Vue d'ensemble"
```

L'application est organisée en **5 couches**:

- API (Controllers) → Mappers → Service → Domain → Infrastructure

### 2. Examiner les endpoints (2 min)

```
Lire: API_REFERENCE.md - Section "Endpoints principaux"
```

Deux controllers principaux:

- `UserController` → Gestion des utilisateurs
- `KycController` → Vérification KYC

### 3. Vous êtes prêt! 🎉

```
Fichier: QUICK_START.md (ce fichier)
```

---

## 📂 Structure du répertoire

```
user-service/
├── src/main/java/com/ebanking/user/
│   ├── api/controller/           ← Endpoints REST
│   ├── api/mapper/               ← Conversion DTO
│   ├── application/service/      ← Logique métier
│   └── domain/model/             ← Entités JPA
│
├── QUICK_START.md               ← Ce fichier
├── ARCHITECTURE.md              ← Design global
├── DEVELOPMENT_GUIDE.md         ← Comment développer
├── API_REFERENCE.md             ← Documentation API
└── RESTRUCTURING_SUMMARY.md     ← Résumé des changements
```

---

## 📖 Documentation - Où trouver quoi?

### Pour comprendre l'architecture

**→ Lire: `ARCHITECTURE.md`**

- Vue d'ensemble du design
- Diagrammes des flux (création utilisateur, KYC)
- Organisation des couches
- Statuts et transitions

### Pour développer une fonctionnalité

**→ Lire: `DEVELOPMENT_GUIDE.md`**

- 6 étapes pour ajouter un endpoint
- Bonnes pratiques
- Exemples de tests
- Checklist avant commit

### Pour utiliser l'API

**→ Lire: `API_REFERENCE.md`**

- Tous les endpoints avec exemples
- Exemples cURL et JavaScript
- Codes d'erreur expliqués
- Cas d'utilisation complets

### Pour les détails des modifications

**→ Lire: `RESTRUCTURING_SUMMARY.md`**

- Liste des changements effectués
- Avant/après comparaison
- Metrics de qualité

---

## 💻 Code Source - Points d'entrée

### Controller - Endpoints REST

**UserController** (`api/controller/UserController.java`)

```
GET    /api/v1/users/me           - Profil utilisateur
GET    /api/v1/users/{userId}     - Profil d'un utilisateur
DELETE /api/v1/users/me           - Supprimer le compte
GET    /api/v1/users/test         - Test
```

**KycController** (`api/controller/KycController.java`)

```
POST   /api/v1/kyc                - Soumettre KYC
GET    /api/v1/kyc/status         - Statut KYC
```

### Service - Logique métier

**UserService** (`application/service/UserService.java`)

- 6 sections logiques claires
- ~30 méthodes organisées
- Chaque méthode documentée

Sections:

```
1. JWT EXTRACTION         (5 méthodes)  - Extraire données du JWT
2. USER MANAGEMENT       (3 méthodes)  - CRUD utilisateur
3. USER PROFILE          (1 méthode)   - Mettre à jour profil
4. KYC MANAGEMENT        (3 méthodes)  - Vérification KYC
5. USER STATUS           (1 méthode)   - Vérifier statut
6. GDPR CONSENT          (1 méthode)   - Gérer consentements
```

### Mappers - Conversion DTO ↔ Entités

**UserMapper** - Converti User ↔ UserRequest/UserResponse
**KycMapper** - Convertir KycVerification ↔ KycResponse
**UserProfileMapper** - Convertir User ↔ UserProfileResponse

### Entités - Modèles JPA

**User** - Utilisateur du système

- Statuts: PENDING_REVIEW, ACTIVE, REJECTED
- Relations: OneToOne avec KycVerification, OneToMany avec GdprConsent

**KycVerification** - Vérification KYC

- Statuts: PENDING_REVIEW, VERIFIED, REJECTED, MORE_INFO_NEEDED
- Stockage: Images en base64

---

## 🧪 Tester les endpoints

### Avec cURL

```bash
# Récupérer le profil
curl -X GET http://localhost:8083/api/v1/users/me \
  -H "Authorization: Bearer {jwt_token}"

# Soumettre KYC
curl -X POST http://localhost:8083/api/v1/kyc \
  -H "Authorization: Bearer {jwt_token}" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+216 50 123 456",
    "cinNumber": "12345678",
    "addressLine1": "123 Rue",
    "city": "Tunis",
    "postalCode": "1000",
    "country": "Tunisia",
    "cinImageBase64": "data:image/png;base64,...",
    "selfieImageBase64": "data:image/png;base64,...",
    "gdprConsents": {"MARKETING": true, "DATA_PROCESSING": true}
  }'
```

### Avec JavaScript/Fetch

```javascript
// Récupérer le profil
fetch("http://localhost:8083/api/v1/users/me", {
  headers: { Authorization: `Bearer ${jwtToken}` },
})
  .then((r) => r.json())
  .then((data) => console.log("Profil:", data));

// Récupérer statut KYC
fetch("http://localhost:8083/api/v1/kyc/status", {
  headers: { Authorization: `Bearer ${jwtToken}` },
})
  .then((r) => r.json())
  .then((data) => console.log("Statut KYC:", data.status));
```

→ Pour plus d'exemples: Voir `API_REFERENCE.md`

---

## 🔄 Flux principaux

### Flux 1: Création d'utilisateur + KYC

```
1. Utilisateur s'authentifie via Keycloak
   ↓
2. Client envoie données KYC à POST /api/v1/kyc
   ↓
3. KycController extrait keycloakId du JWT
   ↓
4. UserService crée User (si n'existe pas)
   ↓
5. UserService.submitKyc():
   - Met à jour profil utilisateur
   - Stocke images (CIN, selfie)
   - Crée KycVerification (status = PENDING_REVIEW)
   - Enregistre consentements GDPR
   ↓
6. Retour 201 CREATED avec détails KYC
```

### Flux 2: Récupérer profil utilisateur

```
1. Client envoie GET /api/v1/users/me + JWT
   ↓
2. UserController.getCurrentUserProfile()
   ↓
3. UserService.getUserByKeycloakIdOptional()
   ↓
4. UserProfileMapper convertir User → UserProfileResponse
   ↓
5. Retour 200 OK avec profil complet
```

---

## 🛠️ Développement

### Ajouter un nouvel endpoint

Suivre les **6 étapes** dans `DEVELOPMENT_GUIDE.md`:

1. Créer le DTO (Request/Response)
2. Créer l'entité JPA (si nécessaire)
3. Ajouter la méthode repository (si nécessaire)
4. Implémenter la logique dans UserService
5. Créer le mapper DTO ↔ Entité
6. Ajouter l'endpoint au controller

### Tester une modification

```bash
# Compiler
./gradlew :user-service:build

# Tester
./gradlew :user-service:test

# Exécuter le service
./gradlew :user-service:bootRun
```

---

## 📋 Checklist développeur

Avant de faire un commit:

```
☐ Code compilé sans erreur
☐ Tests passent (./gradlew test)
☐ JavaDoc complet pour méthodes publiques
☐ Pas de TODO oubliés
☐ Pas de System.out.println()
☐ Validation d'entrée appropriée
☐ Gestion d'erreur appropriée
☐ @Transactional où nécessaire
☐ Code formaté correctement
☐ Imports inutilisés supprimés
```

Voir détails complets dans `DEVELOPMENT_GUIDE.md`

---

## 🔐 Sécurité

- **Authentification**: JWT Keycloak requis
- **Autorisation**: Utilisateur accède seulement à ses données
- **Validation**: DTOs validés avec @Valid
- **Transactions**: @Transactional pour cohérence

---

## 📊 Architecture en couches

```
┌─ API Layer (Controllers) ─────────┐
│  UserController, KycController    │
└──────────┬────────────────────────┘
           │
┌──────────▼────────────────────────┐
│ Mapper Layer (DTO Conversion)      │
│  UserMapper, KycMapper, etc.       │
└──────────┬────────────────────────┘
           │
┌──────────▼────────────────────────┐
│ Service Layer (Logique métier)     │
│  UserService (6 sections)          │
└──────────┬────────────────────────┘
           │
┌──────────▼────────────────────────┐
│ Domain Layer (Entités JPA)         │
│  User, KycVerification, etc.       │
└──────────┬────────────────────────┘
           │
┌──────────▼────────────────────────┐
│ Infrastructure (BD, Kafka, etc.)   │
│  Database, FileStorage, Events     │
└───────────────────────────────────┘
```

→ Voir diagramme détaillé dans `ARCHITECTURE.md`

---

## 🚨 Erreurs courantes

### Erreur: 404 Not Found au get profile

**Cause**: Utilisateur n'existe pas (pas de KYC soumis)
**Solution**: Soumettre d'abord une KYC avant d'accéder au profil

### Erreur: 409 Conflict au soumettre KYC

**Cause**: Une KYC est déjà soumise et en attente
**Solution**: Attendre l'approbation ou le rejet de la KYC

### Erreur: 401 Unauthorized

**Cause**: JWT invalide ou expiré
**Solution**: Renégocier l'authentification avec Keycloak

### Erreur: 400 Bad Request

**Cause**: Validation échouée (champs obligatoires manquants)
**Solution**: Vérifier les champs obligatoires dans `API_REFERENCE.md`

---

## 📞 Support

**Questions sur l'architecture?**
→ Lire `ARCHITECTURE.md`

**Comment développer?**
→ Lire `DEVELOPMENT_GUIDE.md`

**Comment utiliser l'API?**
→ Lire `API_REFERENCE.md`

**Détails des changements?**
→ Lire `RESTRUCTURING_SUMMARY.md`

---

## 🎯 Résumé rapide

| Besoin                 | Fichier              | Temps  |
| ---------------------- | -------------------- | ------ |
| Comprendre design      | ARCHITECTURE.md      | 10 min |
| Apprendre à développer | DEVELOPMENT_GUIDE.md | 20 min |
| Utiliser l'API         | API_REFERENCE.md     | 5 min  |
| Tester un endpoint     | API_REFERENCE.md     | 2 min  |
| Ajouter fonctionnalité | DEVELOPMENT_GUIDE.md | 30 min |

---

## ✅ Vous êtes maintenant prêt!

Vous pouvez:

- ✅ Comprendre l'architecture globale
- ✅ Naviguer dans le code source
- ✅ Tester les endpoints
- ✅ Ajouter une nouvellefonctionnalité
- ✅ Écrire des tests
- ✅ Faire un commit propre

**Bienvenue dans le User Service!** 🚀

---

## 📚 Ressources supplémentaires

- **Spring Boot**: https://spring.io/projects/spring-boot
- **Spring Security**: https://spring.io/projects/spring-security
- **JPA/Hibernate**: https://jakarta.ee/specifications/persistence/
- **MapStruct**: https://mapstruct.org/
- **JWT**: https://jwt.io/

---

**Dernière mise à jour**: Janvier 2024
**Version**: User Service v1.0 (Restructuré)
