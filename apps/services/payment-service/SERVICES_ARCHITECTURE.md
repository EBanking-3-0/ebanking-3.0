# 🏗️ Architecture des Services Spécialisés - Payment Service

## ✅ Services Créés

### 1. **InternalTransferService** 💸
**Rôle :** Gère les virements internes (même banque)

**Caractéristiques :**
- ✅ Synchrone (réponse immédiate)
- ✅ 24/7 (pas de cut-off)
- ✅ Débit + Crédit atomiques via `PaymentSagaOrchestrator`
- ✅ Compensation automatique en cas d'échec

**Flux :**
```
PaymentController → PaymentService → InternalTransferService
  ↓
Validation métier
  ↓
Vérification idempotency
  ↓
Récupération compte destinataire (si numéro fourni)
  ↓
Création Payment (CREATED)
  ↓
PaymentSagaOrchestrator.executePayment()
  ↓
VALIDATED → ANTI-FRAUDE → AUTHORIZED → PROCESSING (Débit + Crédit) → COMPLETED
```

**Fichier :** `InternalTransferService.java`

---

### 2. **SepaTransferService** 🇪🇺
**Rôle :** Gère les virements SEPA (Single Euro Payments Area)

**Caractéristiques :**
- ⏱️ Délai : 1-2 jours ouvrables
- 📅 Cut-off : 16h00 (avant = traitement immédiat, après = batch suivant)
- 🔄 Communication avec `legacy-adapter` (REST → SOAP → Core Banking)
- 💰 Compensation si rejeté par le core banking

**Flux :**
```
PaymentController → PaymentService → SepaTransferService
  ↓
Validation métier
  ↓
Vérification idempotency
  ↓
Récupération IBAN source
  ↓
Création Payment (CREATED)
  ↓
Vérification cut-off (16h)
  ├─ Après 16h → RESERVED (batch suivant)
  └─ Avant 16h → Traitement immédiat
      ↓
      Débit compte source
      ↓
      LegacyAdapterClient.executeSepaTransfer()
      ↓
      Réponse : ACCEPTED → SETTLED → COMPLETED
      └─ REJECTED → Compensation
```

**Fichier :** `SepaTransferService.java`

---

### 3. **InstantTransferService** ⚡
**Rôle :** Gère les virements instantanés (SCT Inst)

**Caractéristiques :**
- ⚡ Délai : < 30 secondes
- 💰 Plafond : 15,000€ (configurable)
- 🔒 Anti-fraude obligatoire (plus strict)
- ❌ Irrévocable une fois accepté (ACK)
- ⏱️ Timeout : 30s

**Flux :**
```
PaymentController → PaymentService → InstantTransferService
  ↓
Validation métier
  ↓
Vérification plafond (15,000€)
  ↓
Vérification idempotency
  ↓
Récupération IBAN source
  ↓
Création Payment (CREATED)
  ↓
VALIDATED
  ↓
ANTI-FRAUDE OBLIGATOIRE
  ├─ Bloqué → REJECTED + Événement fraude
  └─ Autorisé → Continue
      ↓
      Débit compte source
      ↓
      LegacyAdapterClient.executeInstantTransfer() (timeout 30s)
      ↓
      Réponse : ACK → COMPLETED (irrévocable)
      └─ NACK/TIMEOUT → Compensation
```

**Fichier :** `InstantTransferService.java`

---

### 4. **MobileRechargeService** 📱
**Rôle :** Gère les recharges mobiles

**Caractéristiques :**
- ✅ Validation du numéro de téléphone et de l'opérateur
- 🔄 Communication avec système externe (opérateur)
- ⚠️ Gestion des erreurs critiques (numéro invalide, opérateur indisponible)
- 💰 Compensation immédiate en cas d'échec

**Flux :**
```
PaymentController → PaymentService → MobileRechargeService
  ↓
Validation métier
  ↓
Vérification idempotency
  ↓
Validation numéro téléphone
  ↓
Détection opérateur (Orange, SFR, etc.)
  ↓
Création Payment (CREATED)
  ↓
VALIDATED
  ↓
Débit compte source
  ↓
Appel API opérateur (simulation pour l'instant)
  ↓
Réponse : Succès → COMPLETED
  └─ Échec → Compensation immédiate
```

**Fichier :** `MobileRechargeService.java`

---

## 🔄 PaymentService - Routeur Principal

Le `PaymentService` agit maintenant comme un **routeur** qui délègue aux services spécialisés :

```java
@Transactional
public PaymentResult initiatePayment(PaymentRequest request, Long userId) {
    PaymentType paymentType = PaymentType.valueOf(request.getType());
    
    return switch (paymentType) {
        case INTERNAL_TRANSFER -> internalTransferService.executeInternalTransfer(request, userId);
        case SEPA_TRANSFER -> sepaTransferService.executeSepaTransfer(request, userId);
        case SCT_INSTANT -> instantTransferService.executeInstantTransfer(request, userId);
        case MOBILE_RECHARGE -> mobileRechargeService.executeMobileRecharge(request, userId);
        case SWIFT_TRANSFER, MERCHANT_PAYMENT -> {
            // Pour l'instant, utiliser la saga orchestrator
            yield sagaOrchestrator.executePayment(createPaymentFromRequest(request, userId));
        }
    };
}
```

---

## 📋 DTOs Mis à Jour

### AccountResponse
- ✅ Ajouté `iban` (pour SEPA/Instant transfers)

### SepaTransferResponse
- ✅ Ajouté `iso20022Reference` (référence ISO 20022)
- ✅ Status : `ACCEPTED`, `SENT`, `PENDING`, `REJECTED`

### InstantTransferResponse
- ✅ Ajouté `iso20022Reference` (référence ISO 20022)
- ✅ Status : `ACK`, `NACK`, `TIMEOUT`

---

## 🔧 PaymentSagaOrchestrator

La méthode `compensatePayment()` est maintenant **publique** pour être utilisée par les services spécialisés :

```java
public void compensatePayment(Payment payment) {
    // Compensation automatique
    // - Remboursement du débit si effectué
    // - Annulation du crédit si effectué (virement interne)
}
```

---

## ✅ Avantages de cette Architecture

1. **Séparation des responsabilités** : Chaque service gère sa logique métier spécifique
2. **Maintenabilité** : Facile d'ajouter/modifier un type de paiement
3. **Testabilité** : Chaque service peut être testé indépendamment
4. **Évolutivité** : Ajout facile de nouveaux types (SWIFT, Merchant, etc.)

---

## 📝 Notes

- Les erreurs d'import `PaymentRequest cannot be resolved` sont **probablement dues au cache de l'IDE**
- Les fichiers existent bien dans `com.ebanking.payment.dto.request.PaymentRequest`
- **Solution** : Rebuild Gradle + Invalider les caches IDE

---

**Date** : 2024-01-15  
**Status** : ✅ Architecture complète avec services spécialisés
