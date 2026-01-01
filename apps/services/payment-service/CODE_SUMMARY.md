# 📋 Résumé Exécutif - Payment Service

## Vue d'ensemble rapide

Le **Payment Service** est le service central qui gère tous les types de paiements bancaires avec garantie de cohérence via le pattern Saga.

---

## 🗂️ Structure des Fichiers et Rôles

| Fichier | Type | Rôle Principal | Aspect Métier Géré |
|---------|------|---------------|-------------------|
| **PaymentController.java** | Controller | Point d'entrée REST | Exposition APIs paiement |
| **PaymentSagaOrchestrator.java** | Service | Orchestration Saga | Cycle de vie paiement + compensation |
| **InternalTransferService.java** | Service | Virements internes | Virements entre comptes même banque |
| **SepaTransferService.java** | Service | Virements SEPA | Virements européens (1-2 jours) |
| **InstantTransferService.java** | Service | Virements instantanés | SCT Inst (< 30s, irrévocable) |
| **MobileRechargeService.java** | Service | Recharges mobiles | Recharge crédit téléphonique |
| **FraudDetectionService.java** | Service | Détection fraude | Protection anti-fraude |
| **PaymentLimitService.java** | Service | Gestion plafonds | Limites journalières/mensuelles |
| **PaymentStateMachine.java** | Service | Machine à états | Transitions d'état cohérentes |
| **PaymentQueryService.java** | Service | Consultation | Lecture paiements |
| **PaymentEventProducer.java** | Service | Événements Kafka | Publication événements |
| **Payment.java** | Entity | Modèle données | Représentation paiement en DB |
| **PaymentStatus.java** | Enum | États | États possibles d'un paiement |
| **PaymentType.java** | Enum | Types | Types de paiement supportés |
| **PaymentRepository.java** | Repository | Accès données | Requêtes JPA |
| **AccountServiceClient.java** | Client Feign | Communication | Appels account-service |
| **LegacyAdapterClient.java** | Client Feign | Communication | Appels legacy-adapter (SOAP) |
| **AuthServiceClient.java** | Client Feign | Communication | Appels auth-service (MFA) |
| **GlobalExceptionHandler.java** | Exception Handler | Gestion erreurs | Transformation exceptions HTTP |

---

## 🔄 Flux Métier par Type de Paiement

### 💸 Virement Interne

```
Client → Controller → InternalTransferService
  ↓
Vérification idempotency
  ↓
Création Payment (INITIATED)
  ↓
PaymentSagaOrchestrator.executePayment()
  ↓
VALIDATED (solde + limites)
  ↓
ANTI-FRAUDE
  ↓
MFA (si nécessaire)
  ↓
AUTHORIZED
  ↓
PROCESSING (Débit + Crédit)
  ↓
COMPLETED
  ↓
Événement Kafka → notification-service
```

**Caractéristiques :**
- ✅ Synchrone (réponse immédiate)
- ✅ 24/7 (pas de cut-off)
- ✅ Compensation automatique

---

### 🇪🇺 Virement SEPA

```
Client → Controller → SepaTransferService
  ↓
Vérification idempotency
  ↓
Récupération IBAN source
  ↓
Création Payment (INITIATED)
  ↓
Avant 16h ? → Traitement immédiat
Après 16h ? → BATCH_QUEUED
  ↓
Débit compte source
  ↓
Legacy-adapter (REST → SOAP)
  ↓
Réponse : SENT/PENDING/REJECTED
  ↓
Si rejeté → Compensation
```

**Caractéristiques :**
- ⏱️ Délai : 1-2 jours ouvrables
- 📅 Cut-off : 16h
- 🔄 Compensation si rejeté

---

### ⚡ Virement Instantané

```
Client → Controller → InstantTransferService
  ↓
Vérification plafond (15,000€)
  ↓
Vérification idempotency
  ↓
ANTI-FRAUDE OBLIGATOIRE
  ↓
Si bloqué → Exception + Événement fraude
  ↓
Débit compte source
  ↓
Legacy-adapter (timeout 30s)
  ↓
Réponse : ACK/NACK/TIMEOUT
  ↓
ACK → COMPLETED (irrévocable)
NACK/TIMEOUT → Compensation
```

**Caractéristiques :**
- ⚡ < 30 secondes
- 💰 Max 15,000€
- 🔒 Anti-fraude obligatoire
- ❌ Irrévocable (une fois ACK)

---

### 📱 Recharge Mobile

```
Client → Controller → MobileRechargeService
  ↓
Vérification idempotency
  ↓
Détection opérateur (analyse numéro)
  ↓
Normalisation numéro
  ↓
Création Payment (INITIATED)
  ↓
Débit compte source
  ↓
Appel API opérateur
  ↓
Succès → COMPLETED
Échec → Compensation immédiate
```

**Caractéristiques :**
- 📱 Intégration externe (opérateur)
- 🔄 Compensation immédiate si échec

---

## 🛡️ Règles Métier Clés

### 1. Idempotency
- **Clé :** `idempotencyKey` (fournie par client)
- **Vérification :** Avant chaque création
- **Rôle :** Évite doublons en cas de retry

### 2. Anti-Fraude
| Règle | Seuil | Action |
|-------|-------|--------|
| Montant élevé | > 5,000€ | MFA requis |
| Vélocité | > 10 trans/heure | BLOQUÉ |

### 3. Plafonds
| Type | Montant | Période |
|------|---------|---------|
| Journalier | 10,000€ | Par jour |
| Mensuel | 50,000€ | Par mois |
| Instantané | 15,000€ | Par transaction |

### 4. États du Paiement
```
INITIATED → VALIDATED → AUTHORIZED → PROCESSING → COMPLETED
     ↓           ↓            ↓            ↓
   FAILED     FAILED       FAILED    FAILED/COMPENSATED
```

---

## 📊 Communication Inter-Services

### Synchronous (REST/Feign)

| Service | Client | Opérations |
|---------|--------|------------|
| **account-service** | `AccountServiceClient` | getBalance, debit, credit, getAccount |
| **legacy-adapter-service** | `LegacyAdapterClient` | executeSepaTransfer, executeInstantTransfer |
| **auth-service** | `AuthServiceClient` | verifyMFA |

### Asynchronous (Kafka)

| Événement | Producer | Consumers |
|-----------|----------|-----------|
| `transaction.completed` | PaymentService | notification, analytics, audit |
| `payment.failed` | PaymentService | notification, audit |
| `fraud.detected` | PaymentService | notification, audit |

---

## 🎯 Responsabilités par Composant

### Controller
- ✅ Validation des requêtes
- ✅ Extraction métadonnées (IP, User-Agent)
- ✅ Transformation entité → DTO
- ✅ Gestion sécurité (JWT)

### Services Métier
- ✅ Logique métier spécifique par type
- ✅ Gestion idempotency
- ✅ Orchestration via Saga
- ✅ Compensation en cas d'échec

### Saga Orchestrator
- ✅ Orchestration complète du cycle de vie
- ✅ Validation (solde, limites)
- ✅ Anti-fraude
- ✅ MFA
- ✅ Débit/Crédit
- ✅ Compensation

### Services Support
- ✅ Détection fraude
- ✅ Gestion plafonds
- ✅ Machine à états
- ✅ Publication événements

---

## 🔧 Configuration

```yaml
payment:
  fraud:
    high-amount-threshold: 5000.00
    max-transactions-per-hour: 10
  limits:
    daily: 10000.00
    monthly: 50000.00
  instant:
    max-amount: 15000.00
    timeout-seconds: 30
```

---

## 📈 Métriques Importantes

### Performance
- **Virement interne :** < 1 seconde
- **Virement instantané :** < 30 secondes
- **Virement SEPA :** 1-2 jours ouvrables

### Sécurité
- **Taux de fraude bloquée :** Via anti-fraude
- **Taux MFA requis :** Transactions > 5,000€
- **Taux compensation :** Transactions échouées

### Disponibilité
- **Virements internes :** 24/7
- **Virements SEPA :** Cut-off 16h
- **Virements instantanés :** 24/7 (si legacy disponible)

---

## 🚨 Points d'Attention

1. **Compensation critique :** En cas d'échec, la compensation doit toujours réussir
2. **Idempotency :** Clé unique par transaction pour éviter doublons
3. **Irrévocabilité instant :** Une fois ACK, impossible d'annuler
4. **Cut-off SEPA :** Respecter les horaires bancaires
5. **Timeout legacy :** Gérer les timeouts pour virements instantanés

---

## 📚 Documentation Complète

Pour plus de détails, voir : `CODE_ANALYSIS.md`

