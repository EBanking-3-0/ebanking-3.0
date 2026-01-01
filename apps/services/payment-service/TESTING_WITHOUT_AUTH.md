# 🧪 Guide de Test - Payment Service (Sans Authentification)

## ⚠️ Mode Test Activé

L'authentification a été **temporairement désactivée** pour permettre les tests sans Keycloak.

## ✅ Modifications Effectuées

### 1. SecurityConfig.java
- ✅ Tous les endpoints `/api/payments/**` sont maintenant accessibles sans authentification
- ✅ OAuth2 est temporairement commenté

### 2. PaymentController.java
- ✅ Tous les endpoints acceptent maintenant un paramètre `userId` (par défaut: `1`)
- ✅ `@PreAuthorize` est temporairement commenté

### 3. Frontend (payment.service.ts)
- ✅ Toutes les méthodes envoient `userId` en paramètre de requête
- ✅ Par défaut: `userId=1`

### 4. Frontend (payment.component.ts)
- ✅ `currentUserId` est forcé à `1` pour les tests

## 🚀 Comment Tester

### Étape 1 : Vérifier que les données de test sont dans PostgreSQL

```bash
# Exécuter le script SQL
psql -h localhost -p 5432 -U ebanking -d ebanking -f tools/docker/init-test-data.sql
```

Cela crée :
- User ID 1 (John Doe) avec 2 comptes
- User ID 2 (Jane Smith) avec 2 comptes
- Etc.

### Étape 2 : Démarrer les Services

```bash
# Démarrer tous les services nécessaires
# - PostgreSQL
# - Eureka (service discovery)
# - account-service (port 8084)
# - payment-service (port 8085)
# - Frontend (port 4200)
```

### Étape 3 : Tester depuis le Frontend

1. **Accéder à l'application** : http://localhost:4200
2. **Aller dans "💳 Payments"** (pas besoin de se connecter)
3. **Sélectionner un compte** (les comptes de User ID 1 seront affichés)
4. **Remplir le formulaire** et cliquer sur "Send"

### Étape 4 : Tester avec Postman/curl

#### Test Virement Interne

```bash
curl -X POST http://localhost:8085/api/payments/internal?userId=1 \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": 1,
    "toAccountNumber": "FR1420041010050500013M02607",
    "amount": 100.00,
    "currency": "EUR",
    "type": "INTERNAL_TRANSFER",
    "description": "Test payment",
    "idempotencyKey": "test-123456"
  }'
```

#### Test Virement SEPA

```bash
curl -X POST http://localhost:8085/api/payments/sepa?userId=1 \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": 1,
    "toIban": "FR1420041010050500013M02608",
    "beneficiaryName": "Jane Smith",
    "amount": 500.00,
    "currency": "EUR",
    "type": "SEPA_TRANSFER",
    "description": "Test SEPA",
    "idempotencyKey": "test-sepa-123456"
  }'
```

#### Test Virement Instantané

```bash
curl -X POST http://localhost:8085/api/payments/instant?userId=1 \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": 1,
    "toIban": "DE89370400440532013000",
    "beneficiaryName": "Test User",
    "amount": 1000.00,
    "currency": "EUR",
    "type": "SCT_INSTANT",
    "description": "Test Instant",
    "idempotencyKey": "test-instant-123456"
  }'
```

#### Test Recharge Mobile

```bash
curl -X POST http://localhost:8085/api/payments/mobile-recharge?userId=1 \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": 1,
    "phoneNumber": "+33612345678",
    "countryCode": "FR",
    "amount": 20.00,
    "currency": "EUR",
    "type": "MOBILE_RECHARGE",
    "idempotencyKey": "test-mobile-123456"
  }'
```

#### Récupérer les Paiements d'un Utilisateur

```bash
curl -X GET http://localhost:8085/api/payments/user?userId=1
```

## 📊 Données de Test Disponibles

### User ID 1 (John Doe)
- **Compte 1** : ID=1, Numéro=FR1420041010050500013M02606, Solde=10,000€
- **Compte 2** : ID=2, Numéro=FR1420041010050500013M02607, Solde=5,000€

### User ID 2 (Jane Smith)
- **Compte 3** : ID=3, Numéro=FR1420041010050500013M02608, Solde=15,000€
- **Compte 4** : ID=4, Numéro=FR1420041010050500013M02609, Solde=8,000€

## ⚠️ Important

1. **Ceci est temporaire** : Réactivez l'authentification avant la production
2. **Sécurité** : Ne pas utiliser en production sans authentification
3. **Pour réactiver l'auth** : Décommentez les lignes dans `SecurityConfig.java` et `PaymentController.java`

## 🔄 Réactiver l'Authentification

Quand vous voudrez réactiver l'authentification :

1. Dans `SecurityConfig.java` :
   - Remplacez `.permitAll()` par `.authenticated()` pour `/api/payments/**`
   - Décommentez `.oauth2ResourceServer(...)`

2. Dans `PaymentController.java` :
   - Décommentez `@PreAuthorize("hasRole('user')")`
   - Remplacez `@RequestParam userId` par `@AuthenticationPrincipal JwtAuthenticationToken auth`
   - Utilisez `extractUserId(auth)` au lieu du paramètre

3. Dans le frontend :
   - Remettez le code original dans `payment.component.ts`
   - Supprimez les paramètres `userId` dans `payment.service.ts`

## ✅ Vérification

Pour vérifier que tout fonctionne :

1. ✅ Les comptes s'affichent dans le frontend
2. ✅ Les paiements peuvent être créés
3. ✅ L'historique des paiements s'affiche
4. ✅ Les paiements sont enregistrés avec le bon userId
