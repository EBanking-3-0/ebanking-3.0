# 📊 Analyse Complète du Code - Payment Service

## Vue d'ensemble

Le **Payment Service** est le cœur du système de paiement bancaire. Il orchestre tous les types de virements et garantit la cohérence des transactions distribuées via le pattern Saga.

---

## 🏗️ Architecture des Composants

### 1. **CONTROLLER LAYER** - Point d'entrée HTTP

#### `PaymentController.java`
**Rôle technique :** Point d'entrée REST pour toutes les opérations de paiement  
**Rôle métier :** Exposition des APIs de paiement aux clients (web, mobile)

**Méthodes et responsabilités :**

| Méthode | Endpoint | Rôle Métier | Détails |
|---------|----------|-------------|---------|
| `createInternalTransfer()` | `POST /api/payments/internal` | Virement interne entre comptes | Point d'entrée pour les virements entre comptes de la même banque |
| `createSepaTransfer()` | `POST /api/payments/sepa` | Virement SEPA européen | Point d'entrée pour les virements SEPA (1-2 jours) |
| `createInstantTransfer()` | `POST /api/payments/instant` | Virement instantané (SCT Inst) | Point d'entrée pour les virements instantanés (< 30s) |
| `createMobileRecharge()` | `POST /api/payments/mobile-recharge` | Recharge mobile | Point d'entrée pour recharger un téléphone |
| `getPayment()` | `GET /api/payments/{id}` | Consultation paiement | Récupère les détails d'un paiement |
| `getUserPayments()` | `GET /api/payments/user` | Historique paiements | Liste tous les paiements d'un utilisateur |

**Sécurité :**
- `@PreAuthorize("hasRole('USER')")` : Toutes les opérations nécessitent une authentification
- Extraction de l'IP et User-Agent pour traçabilité et anti-fraude

**Transformation :**
- `toResponse()` : Convertit l'entité `Payment` en DTO `PaymentResponse` pour l'API
- `buildMessage()` : Génère des messages métier selon le statut du paiement

---

### 2. **SERVICE LAYER** - Logique Métier

#### A. **PaymentSagaOrchestrator.java** ⭐ CŒUR DU SYSTÈME
**Rôle technique :** Orchestrateur de Saga pour transactions distribuées  
**Rôle métier :** Gère le cycle de vie complet d'un paiement avec compensation

**Pattern utilisé :** Saga Orchestrée (Choreography)

**États du paiement (State Machine) :**
```
INITIATED → VALIDATED → AUTHORIZED → PROCESSING → COMPLETED
                ↓            ↓            ↓
              FAILED       FAILED       FAILED/COMPENSATED
```

**Méthodes principales :**

1. **`executePayment(Payment payment)`**
   - **Rôle métier :** Orchestre toute la saga de paiement
   - **Étapes :**
     - ✅ INITIATED : Création du paiement
     - ✅ VALIDATED : Vérification solde + limites
     - ✅ ANTI-FRAUDE : Détection de fraude
     - ✅ MFA : Authentification forte si nécessaire
     - ✅ AUTHORIZED : Autorisation du paiement
     - ✅ PROCESSING : Débit + Crédit
     - ✅ COMPLETED : Succès
     - ❌ COMPENSATED : En cas d'échec, annulation et remboursement

2. **`validatePayment(Payment payment)`**
   - **Rôle métier :** Validation préalable avant traitement
   - Vérifie :
     - Solde disponible suffisant
     - Limites journalières (défaut: 10,000€)
     - Limites mensuelles (défaut: 50,000€)
     - Statut du compte (ACTIVE)

3. **`verifyMFA(Payment payment)`**
   - **Rôle métier :** Authentification forte pour paiements sensibles
   - Appelle `auth-service` pour vérifier le code MFA
   - Nécessaire si :
     - Montant élevé (> 5,000€)
     - Détection de risque par anti-fraude

4. **`processPayment(Payment payment)`**
   - **Rôle métier :** Exécution du paiement
   - **Pour virement interne :**
     1. Débite le compte source via `account-service`
     2. Crédite le compte destination via `account-service`
   - **Gestion d'erreur :** Si crédit échoue, compensation automatique

5. **`compensatePayment(Payment payment)`**
   - **Rôle métier :** Compensation (rollback) en cas d'échec
   - **Logique :**
     - Si débit effectué → Remboursement (crédit)
     - Si crédit effectué → Annulation (débit inverse)
   - **Critique :** Garantit la cohérence financière

---

#### B. **InternalTransferService.java**
**Rôle technique :** Service dédié aux virements internes  
**Rôle métier :** Virements entre comptes de la même banque (instantanés)

**Méthode principale :**
- `executeInternalTransfer(InternalTransferRequest, Long userId)`

**Logique métier :**
1. **Idempotency check** : Vérifie si le paiement existe déjà (évite les doublons)
2. **Création Payment** : Crée l'entité avec statut `INITIATED`
3. **Délégation à Saga** : Appelle `PaymentSagaOrchestrator.executePayment()`

**Caractéristiques :**
- ✅ Traitement synchrone (réponse immédiate)
- ✅ Pas de cut-off (disponible 24/7)
- ✅ Compensation automatique en cas d'échec

---

#### C. **SepaTransferService.java**
**Rôle technique :** Service dédié aux virements SEPA  
**Rôle métier :** Virements européens (SEPA Credit Transfer)

**Méthode principale :**
- `executeSepaTransfer(SepaTransferRequest, Long userId)`

**Logique métier :**

1. **Idempotency check**
2. **Récupération IBAN source** depuis `account-service`
3. **Gestion du cut-off** :
   - **Avant 16h** : Traitement immédiat
   - **Après 16h** : Mise en file d'attente batch (`BATCH_QUEUED`)

4. **`processSepaImmediate(Payment)`** :
   - Débite le compte source
   - Envoie au `legacy-adapter` (transformation REST → SOAP)
   - Traite la réponse (SENT/PENDING/REJECTED)
   - Si rejeté → Compensation

**Caractéristiques :**
- ⏱️ Délai : 1-2 jours ouvrables
- 📅 Cut-off : 16h (traitement batch après)
- 🔄 Compensation : Remboursement si échec legacy

---

#### D. **InstantTransferService.java** ⚡
**Rôle technique :** Service dédié aux virements instantanés  
**Rôle métier :** Virements instantanés (SCT Inst - Single Euro Payments Area Instant Credit Transfer)

**Méthode principale :**
- `executeInstantTransfer(InstantTransferRequest, Long userId)`

**Logique métier :**

1. **Vérification plafond** : Max 15,000€ (réglementation SCT Inst)
2. **Anti-fraude obligatoire** : Vérification temps réel (pas de MFA, blocage direct si fraude)
3. **Débit immédiat**
4. **Appel legacy avec timeout** : 30 secondes max
5. **Traitement ACK/NACK** :
   - **ACK** : Succès, paiement irrévocable
   - **NACK** : Rejet, compensation immédiate
   - **TIMEOUT** : Compensation + investigation

**Caractéristiques :**
- ⚡ Délai : < 30 secondes
- 💰 Plafond : 15,000€
- 🔒 Anti-fraude : Obligatoire et temps réel
- ❌ Irrévocable : Une fois ACK reçu, impossible d'annuler

---

#### E. **MobileRechargeService.java** 📱
**Rôle technique :** Service dédié aux recharges mobiles  
**Rôle métier :** Recharge de crédit téléphonique

**Méthode principale :**
- `executeMobileRecharge(MobileRechargeRequest, Long userId)`

**Logique métier :**

1. **Détection opérateur** : Analyse le numéro pour identifier l'opérateur (Orange, SFR, Bouygues, Free)
2. **Normalisation numéro** : Format international (+33...)
3. **Débit compte**
4. **Appel API opérateur** : Envoie la demande de recharge
5. **Gestion résultat** :
   - Succès → COMPLETED
   - Échec → Compensation immédiate

**Caractéristiques :**
- 📱 Intégration externe : Appel API opérateur
- 🔄 Compensation : Immédiate en cas d'échec
- ⚠️ Risque : Si débit OK mais recharge échoue, compensation nécessaire

---

#### F. **FraudDetectionService.java** 🛡️
**Rôle technique :** Détection de fraude  
**Rôle métier :** Protection contre les transactions frauduleuses

**Méthode principale :**
- `checkFraud(Payment payment)`

**Règles de détection :**

1. **Montant élevé** :
   - Seuil : 5,000€ (configurable)
   - Action : Requiert MFA

2. **Vélocité (High Velocity)** :
   - Seuil : > 10 transactions/heure (configurable)
   - Action : **BLOQUÉ** (fraud probable)

**Résultats possibles :**
- `allowed()` : Transaction autorisée
- `requireMFA()` : Nécessite authentification forte
- `blocked()` : Transaction bloquée (fraud détectée)

**Indicateurs de fraude :**
- `HIGH_AMOUNT` : Montant supérieur au seuil
- `HIGH_VELOCITY` : Trop de transactions en peu de temps

---

#### G. **PaymentLimitService.java** 📊
**Rôle technique :** Gestion des plafonds de paiement  
**Rôle métier :** Limitation des montants pour sécurité et conformité

**Méthodes :**

1. **`checkDailyLimit(Long accountId, BigDecimal amount)`**
   - **Rôle métier :** Vérifie le plafond journalier
   - **Seuil par défaut :** 10,000€
   - **Calcul :** Somme des paiements COMPLETED depuis minuit
   - **Exception :** `DailyLimitExceededException` si dépassement

2. **`checkMonthlyLimit(Long accountId, BigDecimal amount)`**
   - **Rôle métier :** Vérifie le plafond mensuel
   - **Seuil par défaut :** 50,000€
   - **Calcul :** Somme des paiements COMPLETED depuis le 1er du mois
   - **Exception :** `MonthlyLimitExceededException` si dépassement

**Utilisation :** Appelé dans `PaymentSagaOrchestrator.validatePayment()`

---

#### H. **PaymentStateMachine.java** 🔄
**Rôle technique :** Machine à états pour les paiements  
**Rôle métier :** Garantit la cohérence des transitions d'état

**Méthode principale :**
- `transition(Payment payment, PaymentStatus newStatus)`

**Transitions autorisées :**

```
null → INITIATED ✅
INITIATED → VALIDATED ✅
INITIATED → FAILED ✅
VALIDATED → AUTHORIZED ✅
VALIDATED → FAILED ✅
AUTHORIZED → PROCESSING ✅
AUTHORIZED → FAILED ✅
PROCESSING → COMPLETED ✅
PROCESSING → FAILED ✅
PROCESSING → COMPENSATED ✅
```

**Rôle métier :**
- Empêche les transitions invalides (ex: COMPLETED → PROCESSING)
- Garantit la traçabilité des changements d'état
- Log toutes les transitions pour audit

---

#### I. **PaymentQueryService.java** 🔍
**Rôle technique :** Service de consultation  
**Rôle métier :** Lecture des paiements (pas de modification)

**Méthodes :**

1. **`getPayment(Long paymentId)`**
   - Récupère un paiement par ID
   - Exception : `PaymentNotFoundException` si inexistant

2. **`getUserPayments(Long userId)`**
   - Liste tous les paiements d'un utilisateur
   - Tri : Plus récents en premier

3. **`getPaymentByTransactionId(String transactionId)`**
   - Recherche par transaction ID (UUID)
   - Utile pour réconciliation

---

#### J. **PaymentEventProducer.java** 📢
**Rôle technique :** Publication d'événements Kafka  
**Rôle métier :** Notification asynchrone des autres services

**Méthodes :**

1. **`publishTransactionCompleted(Payment payment)`**
   - **Consommateurs :** `notification-service`, `analytics-service`, `audit-service`
   - **Événement :** `TransactionCompletedEvent`

2. **`publishPaymentFailed(Payment payment, Exception e)`**
   - **Consommateurs :** `notification-service`, `audit-service`
   - **Événement :** `PaymentFailedEvent`

3. **`publishFraudDetected(Payment payment, ...)`**
   - **Consommateurs :** `notification-service`, `audit-service`
   - **Événement :** `FraudDetectedEvent`

**Architecture :** Wrapper autour de `TypedEventProducer` (librairie partagée)

---

### 3. **ENTITY LAYER** - Modèle de données

#### `Payment.java`
**Rôle technique :** Entité JPA représentant un paiement  
**Rôle métier :** Modèle de données complet d'un paiement

**Champs principaux :**

| Champ | Type | Rôle Métier |
|-------|------|-------------|
| `id` | Long | Identifiant unique |
| `transactionId` | String (UUID) | Identifiant transaction (pour réconciliation) |
| `idempotencyKey` | String | Clé d'idempotence (évite doublons) |
| `paymentType` | Enum | Type : INTERNAL, SEPA, INSTANT, MOBILE_RECHARGE |
| `status` | Enum | État actuel du paiement |
| `fromAccountId` | Long | Compte source |
| `toAccountId` | Long | Compte destination (si interne) |
| `fromIban` | String | IBAN source (pour SEPA/Instant) |
| `toIban` | String | IBAN destination |
| `amount` | BigDecimal | Montant (précision 19,4) |
| `currency` | String | Devise (EUR par défaut) |
| `userId` | Long | Utilisateur initiateur |
| `description` | String | Libellé du paiement |
| `phoneNumber` | String | Numéro téléphone (recharge mobile) |
| `operatorCode` | String | Opérateur (ORANGE, SFR, etc.) |
| `debitTransactionId` | String | ID transaction débit (account-service) |
| `creditTransactionId` | String | ID transaction crédit (account-service) |
| `externalTransactionId` | String | ID transaction legacy (core bancaire) |
| `mfaVerified` | Boolean | MFA validé |
| `fraudCheckPassed` | Boolean | Anti-fraude passé |
| `ipAddress` | String | IP client (audit) |
| `userAgent` | String | User-Agent (audit) |
| `failureReason` | String | Raison d'échec |
| `estimatedCompletionDate` | Instant | Date estimée de complétion |
| `completedAt` | Instant | Date de complétion réelle |
| `createdAt` | Instant | Date de création |
| `updatedAt` | Instant | Date de dernière modification |

**Lifecycle hooks :**
- `@PrePersist` : Initialise `createdAt` et `updatedAt`
- `@PreUpdate` : Met à jour `updatedAt`

---

#### `PaymentStatus.java` (Enum)
**Rôle métier :** États possibles d'un paiement

```java
INITIATED      // Paiement créé, en attente de validation
VALIDATED      // Validé (solde OK, limites OK)
AUTHORIZED     // Autorisé (MFA OK si nécessaire)
PROCESSING     // En cours de traitement
COMPLETED      // Terminé avec succès
FAILED         // Échoué
COMPENSATED    // Annulé et remboursé
CANCELLED      // Annulé manuellement
BATCH_QUEUED   // En file d'attente batch (SEPA après cut-off)
SENT           // Envoyé au legacy (SEPA)
```

---

#### `PaymentType.java` (Enum)
**Rôle métier :** Types de paiement supportés

```java
INTERNAL_TRANSFER    // Virement interne (même banque)
SEPA_TRANSFER        // Virement SEPA (Europe)
INSTANT_TRANSFER     // Virement instantané (SCT Inst)
MOBILE_RECHARGE      // Recharge mobile
```

---

### 4. **REPOSITORY LAYER** - Accès données

#### `PaymentRepository.java`
**Rôle technique :** Interface JPA Repository  
**Rôle métier :** Requêtes sur les paiements

**Méthodes :**

1. **`findByIdempotencyKey(String key)`**
   - **Rôle métier :** Vérifie l'idempotence
   - **Utilisation :** Évite les doublons en cas de retry

2. **`findByTransactionId(String transactionId)`**
   - **Rôle métier :** Recherche par transaction ID
   - **Utilisation :** Réconciliation

3. **`findByUserIdOrderByCreatedAtDesc(Long userId)`**
   - **Rôle métier :** Historique utilisateur
   - **Tri :** Plus récents en premier

4. **`countRecentTransfers(Long accountId, Instant since)`**
   - **Rôle métier :** Compte les transactions récentes
   - **Utilisation :** Détection de vélocité (anti-fraude)

5. **`sumAmountSince(Long accountId, Instant since)`**
   - **Rôle métier :** Somme des montants depuis une date
   - **Utilisation :** Calcul des plafonds journaliers/mensuels

---

### 5. **CLIENT LAYER** - Communication inter-services

#### `AccountServiceClient.java` (Feign)
**Rôle technique :** Client REST pour account-service  
**Rôle métier :** Opérations sur les comptes

**Méthodes :**

1. **`getBalance(Long accountId)`**
   - **Rôle métier :** Récupère le solde disponible
   - **Utilisation :** Vérification avant paiement

2. **`getAccount(Long accountId)`**
   - **Rôle métier :** Récupère les infos du compte (IBAN, etc.)
   - **Utilisation :** Récupération IBAN pour SEPA/Instant

3. **`debit(Long accountId, DebitRequest)`**
   - **Rôle métier :** Débite un compte
   - **Utilisation :** Débit du compte source

4. **`credit(Long accountId, CreditRequest)`**
   - **Rôle métier :** Crédite un compte
   - **Utilisation :** Crédit du compte destination + compensation

---

#### `LegacyAdapterClient.java` (Feign)
**Rôle technique :** Client REST pour legacy-adapter-service  
**Rôle métier :** Communication avec le core bancaire (via SOAP)

**Méthodes :**

1. **`executeSepaTransfer(SepaTransferRequest)`**
   - **Rôle métier :** Envoie un virement SEPA au core bancaire
   - **Transformation :** REST → SOAP (fait par legacy-adapter)
   - **Réponse :** SENT, PENDING, ou REJECTED

2. **`executeInstantTransfer(InstantTransferRequest)`**
   - **Rôle métier :** Envoie un virement instantané au core bancaire
   - **Réponse :** ACK (succès), NACK (rejet), ou TIMEOUT

---

#### `AuthServiceClient.java` (Feign)
**Rôle technique :** Client REST pour auth-service  
**Rôle métier :** Authentification forte (MFA)

**Méthode :**

1. **`verifyMFA(MfaVerificationRequest)`**
   - **Rôle métier :** Vérifie le code MFA
   - **Utilisation :** Pour paiements sensibles (montant élevé, risque fraude)

---

### 6. **EXCEPTION LAYER** - Gestion d'erreurs

#### `GlobalExceptionHandler.java`
**Rôle technique :** Handler global des exceptions  
**Rôle métier :** Transformation des exceptions en réponses HTTP cohérentes

**Exceptions gérées :**

| Exception | HTTP Status | Rôle Métier |
|-----------|-------------|-------------|
| `InsufficientFundsException` | 400 | Solde insuffisant |
| `DailyLimitExceededException` | 400 | Plafond journalier dépassé |
| `MonthlyLimitExceededException` | 400 | Plafond mensuel dépassé |
| `FraudDetectedException` | 403 | Fraude détectée |
| `PaymentNotFoundException` | 404 | Paiement introuvable |
| `MfaVerificationFailedException` | 401 | MFA échoué |
| `SepaRejectionException` | 400 | Rejet SEPA par legacy |
| `InstantTransferRejectedException` | 400 | Rejet virement instantané |
| `OperatorRechargeException` | 400 | Échec recharge opérateur |
| `PaymentProcessingException` | 500 | Erreur traitement |
| `InvalidStateTransitionException` | 400 | Transition d'état invalide |

**Format de réponse :**
```json
{
  "status": 400,
  "error": "INSUFFICIENT_FUNDS",
  "message": "Solde insuffisant. Disponible: 100.00, Requis: 500.00",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

### 7. **DTO LAYER** - Transfert de données

#### Request DTOs

**`InternalTransferRequest.java`**
- `fromAccountId` : Compte source
- `toAccountId` : Compte destination
- `amount` : Montant
- `currency` : Devise
- `description` : Libellé
- `idempotencyKey` : Clé idempotence
- `ipAddress` / `userAgent` : Métadonnées (ajoutées par controller)

**`SepaTransferRequest.java`**
- `fromAccountId` : Compte source
- `toIban` : IBAN destination (validation regex)
- `amount` : Montant
- `currency` : Devise
- `description` : Libellé
- `idempotencyKey` : Clé idempotence

**`InstantTransferRequest.java`**
- Similaire à SEPA mais avec validation montant max (15,000€)

**`MobileRechargeRequest.java`**
- `fromAccountId` : Compte source
- `phoneNumber` : Numéro (validation regex)
- `countryCode` : Code pays
- `amount` : Montant
- `currency` : Devise
- `idempotencyKey` : Clé idempotence

#### Response DTOs

**`PaymentResponse.java`**
- `paymentId` : ID paiement
- `transactionId` : UUID transaction
- `status` : Statut (string)
- `amount` : Montant
- `currency` : Devise
- `message` : Message métier (généré selon statut)
- `createdAt` : Date création
- `estimatedCompletionDate` : Date estimée complétion

**`PaymentResult.java`**
- `success` : Boolean succès
- `payment` : Entité Payment
- `errorMessage` : Message d'erreur (si échec)

---

## 🔄 Flux Métier Complets

### Flux 1 : Virement Interne

```
1. Client → PaymentController.createInternalTransfer()
2. Controller → InternalTransferService.executeInternalTransfer()
3. Service vérifie idempotency
4. Service crée Payment (INITIATED)
5. Service → PaymentSagaOrchestrator.executePayment()
6. Saga VALIDATED : Vérifie solde + limites
7. Saga ANTI-FRAUDE : Détecte fraude
8. Saga MFA (si nécessaire) : Vérifie MFA
9. Saga AUTHORIZED : Autorise
10. Saga PROCESSING : Débite compte source + Crédite compte destination
11. Saga COMPLETED : Publie événement Kafka
12. Controller retourne PaymentResponse
```

**Compensation si échec :**
- Si crédit échoue après débit → Remboursement automatique

---

### Flux 2 : Virement SEPA

```
1. Client → PaymentController.createSepaTransfer()
2. Controller → SepaTransferService.executeSepaTransfer()
3. Service vérifie idempotency
4. Service récupère IBAN source (account-service)
5. Service crée Payment (INITIATED)
6. Si avant 16h :
   a. Débite compte source
   b. Envoie au legacy-adapter (REST → SOAP)
   c. Traite réponse (SENT/PENDING/REJECTED)
   d. Si rejeté → Compensation
7. Si après 16h :
   a. Statut BATCH_QUEUED
   b. Traitement le lendemain
```

**Caractéristiques :**
- Délai : 1-2 jours ouvrables
- Cut-off : 16h
- Compensation : Si rejeté par legacy

---

### Flux 3 : Virement Instantané

```
1. Client → PaymentController.createInstantTransfer()
2. Controller → InstantTransferService.executeInstantTransfer()
3. Service vérifie plafond (15,000€)
4. Service vérifie idempotency
5. Service → FraudDetectionService.checkFraud() (OBLIGATOIRE)
6. Si bloqué → Exception + Publication événement fraude
7. Si OK → Débite compte source
8. Service envoie au legacy-adapter (timeout 30s)
9. Traite réponse :
   - ACK → COMPLETED (irrévocable)
   - NACK → Compensation
   - TIMEOUT → Compensation + Investigation
```

**Caractéristiques :**
- Délai : < 30 secondes
- Anti-fraude : Obligatoire
- Irrévocable : Une fois ACK, impossible d'annuler

---

### Flux 4 : Recharge Mobile

```
1. Client → PaymentController.createMobileRecharge()
2. Controller → MobileRechargeService.executeMobileRecharge()
3. Service vérifie idempotency
4. Service détecte opérateur (analyse numéro)
5. Service normalise numéro (format international)
6. Service crée Payment (INITIATED)
7. Service débite compte source
8. Service appelle API opérateur
9. Si succès → COMPLETED
10. Si échec → Compensation immédiate
```

**Caractéristiques :**
- Intégration externe : API opérateur
- Compensation : Immédiate si échec

---

## 🛡️ Sécurité & Conformité

### 1. **Idempotency**
- **Clé :** `idempotencyKey` (fournie par client)
- **Vérification :** Avant chaque création de paiement
- **Rôle :** Évite les doublons en cas de retry réseau

### 2. **Anti-Fraude**
- **Montant élevé :** > 5,000€ → MFA requis
- **Vélocité :** > 10 transactions/heure → BLOQUÉ
- **Obligatoire :** Pour virements instantanés

### 3. **Plafonds**
- **Journalier :** 10,000€ (configurable)
- **Mensuel :** 50,000€ (configurable)
- **Instantané :** 15,000€ (réglementation SCT Inst)

### 4. **Audit**
- **IP Address :** Enregistrée pour chaque paiement
- **User-Agent :** Enregistré pour traçabilité
- **Événements Kafka :** Tous les événements publiés pour audit-service

### 5. **MFA (Multi-Factor Authentication)**
- **Déclenchement :** Montant élevé ou risque fraude
- **Vérification :** Via auth-service
- **Échec :** Transaction bloquée

---

## 📊 Métriques & Observabilité

### Événements Kafka publiés :

1. **`transaction.completed`**
   - **Consommateurs :** notification-service, analytics-service, audit-service
   - **Contenu :** Détails transaction complète

2. **`payment.failed`**
   - **Consommateurs :** notification-service, audit-service
   - **Contenu :** Raison d'échec, erreur

3. **`fraud.detected`**
   - **Consommateurs :** notification-service, audit-service
   - **Contenu :** Type fraude, sévérité, indicateurs

---

## 🔧 Configuration

### Variables d'environnement :

```yaml
payment:
  fraud:
    high-amount-threshold: 5000.00      # Seuil montant élevé
    max-transactions-per-hour: 10        # Seuil vélocité
  limits:
    daily: 10000.00                      # Plafond journalier
    monthly: 50000.00                    # Plafond mensuel
  instant:
    max-amount: 15000.00                 # Plafond instantané
    timeout-seconds: 30                  # Timeout legacy
```

---

## 🎯 Points Clés Métier

1. **Saga Pattern** : Garantit la cohérence des transactions distribuées
2. **Compensation** : Rollback automatique en cas d'échec
3. **Idempotency** : Évite les doublons
4. **Anti-Fraude** : Protection temps réel
5. **Plafonds** : Conformité réglementaire
6. **Cut-off SEPA** : Respect des horaires bancaires
7. **Irrévocabilité Instant** : Conformité SCT Inst
8. **Audit complet** : Traçabilité totale

---

## 📝 Notes d'Implémentation

### Améliorations possibles :

1. **Circuit Breaker** : Pour appels legacy-adapter
2. **Retry Policy** : Pour appels externes (opérateurs)
3. **Saga State Store** : Persistance état saga pour récupération
4. **Outbox Pattern** : Garantir publication événements
5. **Distributed Lock** : Pour éviter race conditions
6. **Rate Limiting** : Limitation requêtes par utilisateur

---

**Document généré le :** 2024-01-15  
**Version :** 1.0  
**Service :** Payment Service (Port 8085)

