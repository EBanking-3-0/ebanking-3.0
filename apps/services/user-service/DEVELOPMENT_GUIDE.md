# Guide de développement - User Service

## Introduction

Ce guide décrit comment développer, tester et déployer des modifications dans le User Service.

## Structure des packages

### 1. **Controller Layer** (`api/controller/`)

Responsabilité: Exposer les endpoints REST

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    // - Valider les paramètres
    // - Extraire l'authentification
    // - Appeler le service métier
    // - Mapper la réponse
    // - Gérer les erreurs HTTP
}
```

**Règles**:
- Injecter uniquement le Service (pas le Repository)
- Valider avec `@Valid`
- Mapper les erreurs en codes HTTP appropriés
- Documenter avec JavaDoc

### 2. **Mapper Layer** (`api/mapper/`)

Responsabilité: Convertir entre Entités et DTOs

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    // DTO → Entité (pour POST/PUT)
    User toEntity(UserRequest request);
    
    // Entité → DTO (pour GET)
    UserResponse toResponse(User user);
}
```

**Règles**:
- Utiliser MapStruct (pas de logique métier)
- Documenter les mappages complexes
- Tester les mappages

### 3. **Service Layer** (`application/service/`)

Responsabilité: Logique métier et orchestration

```java
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    // Organiser en sections logiques:
    
    // ==================== JWT EXTRACTION ====================
    // Extraire les données du JWT Keycloak
    
    // ==================== USER MANAGEMENT ====================
    // CRUD utilisateurs et profil
    
    // ==================== KYC MANAGEMENT ====================
    // Vérification KYC
    
    // ==================== GDPR MANAGEMENT ====================
    // Consentements GDPR
}
```

**Règles**:
- Une responsabilité par méthode
- Documenter les pré/post-conditions
- Utiliser les transactions
- Lever des exceptions métier appropriées
- Organiser en sections commentées

### 4. **Domain Model Layer** (`domain/`)

Responsabilité: Représenter les concepts métier

```java
@Entity
@Table(name = "users")
public class User {
    // - @Id, @GeneratedValue
    // - @Column pour les propriétés
    // - @Enumerated pour les enums
    // - Relations (@OneToOne, @OneToMany)
    // - Cascade rules
    // - Fetch strategies
}
```

**Règles**:
- Documenter chaque champ
- Utiliser Lombok (@Getter, @Setter, @Builder)
- Cascade appropriées (ALL ou PERSIST)
- Lazy loading par défaut

### 5. **Repository Layer** (`domain/repository/`)

Responsabilité: Accès aux données

```java
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByKeycloakId(String keycloakId);
}
```

**Règles**:
- Utiliser JpaRepository
- Nommage cohérent des méthodes
- Pas de logique métier
- Index les colonnes recherchées fréquemment

## Ajouter un nouvel endpoint

### Étape 1: Définir le DTO

Fichier: `libs/shared/dto/src/.../YourRequest.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class YourRequest {
    @NotBlank
    private String field1;
    
    @NotNull
    private Integer field2;
}
```

### Étape 2: Créer l'entité (si nécessaire)

```java
@Entity
@Table(name = "your_table")
public class YourEntity {
    @Id
    @GeneratedValue
    private UUID id;
    
    @Column(nullable = false)
    private String field1;
}
```

### Étape 3: Ajouter le Repository (si nécessaire)

```java
@Repository
public interface YourRepository extends JpaRepository<YourEntity, UUID> {
    Optional<YourEntity> findByField(String field);
}
```

### Étape 4: Ajouter la logique métier au Service

```java
@Transactional
public YourResponse createYour(YourRequest request, Authentication auth) {
    // 1. Valider l'authentification
    String keycloakId = getKeycloakIdFromJwt(auth);
    User user = getUserByKeycloakIdOptional(keycloakId);
    
    // 2. Logique métier
    YourEntity entity = new YourEntity();
    entity.setField1(request.getField1());
    
    // 3. Persister
    repository.save(entity);
    
    // 4. Retourner la réponse
    return mapper.toResponse(entity);
}
```

### Étape 5: Ajouter l'endpoint au Controller

```java
@PostMapping("/your-endpoint")
public ResponseEntity<YourResponse> createYour(
        @Valid @RequestBody YourRequest request,
        Authentication authentication) {
    YourResponse response = userService.createYour(request, authentication);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

### Étape 6: Ajouter le Mapper (si nécessaire)

```java
@Mapper(componentModel = "spring")
public interface YourMapper {
    YourResponse toResponse(YourEntity entity);
}
```

## Bonnes pratiques

### 1. Documentation

Chaque classe et méthode publique doit avoir :

```java
/**
 * Description brève
 * 
 * Détail plus long si nécessaire
 * 
 * @param param1 Description du paramètre
 * @return Description de la valeur retournée
 * @throws Exception Quand l'exception est levée
 */
public ResponseEntity<Response> method(String param1) {
```

### 2. Validation

```java
// Utiliser la validation Jakarta/Bean Validation
@PostMapping
public ResponseEntity<Response> create(@Valid @RequestBody Request request) {
    // request.field est garanti d'être non-null et valide
}

// Ou valider manuellement
if (value < 0) {
    throw new IllegalArgumentException("Value must be positive");
}
```

### 3. Gestion d'erreurs

```java
@PostMapping
public ResponseEntity<YourResponse> submit(
        @Valid @RequestBody YourRequest request,
        Authentication authentication) {
    try {
        return ResponseEntity.ok(service.submit(request, authentication));
    } catch (IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
```

### 4. Sections de code

Organiser le service en sections logiques avec commentaires :

```java
@Service
public class UserService {
    // ==================== JWT EXTRACTION ====================
    public String getKeycloakIdFromJwt(Authentication auth) { }
    
    // ==================== USER MANAGEMENT ====================
    public User getUserByKeycloakId(String keycloakId) { }
    
    // ==================== KYC MANAGEMENT ====================
    public KycVerification submitKyc(User user, KycRequest request) { }
}
```

### 5. Transactions

```java
// Pour les opérations lecture+écriture
@Transactional
public User updateProfile(User user, ProfileRequest request) {
    user.setField(request.getField());
    return repository.save(user);
}

// Pour les lectures uniquement
@Transactional(readOnly = true)
public User getById(UUID id) {
    return repository.findById(id).orElse(null);
}
```

### 6. Lazy vs Eager Loading

```java
// Lazy loading (par défaut, recommandé)
@OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
private KycVerification kycVerification;

// Eager loading (seulement si nécessaire)
@ManyToOne(fetch = FetchType.EAGER)
private User user;
```

### 7. Cascade

```java
// Cascade.ALL: supprimer la relation supprime l'entité
@OneToOne(cascade = CascadeType.ALL)

// Cascade.PERSIST: créer la relation crée aussi l'entité
@OneToOne(cascade = CascadeType.PERSIST)

// Orphan removal: si suppression de la relation, supprimer l'entité
@OneToOne(orphanRemoval = true)
```

## Testing

### Tester un Mapper

```java
@ExtendWith(MockitoExtension.class)
class UserMapperTest {
    
    @InjectMocks
    private UserMapper mapper;
    
    @Test
    void testToResponse() {
        User user = User.builder()
            .id(UUID.randomUUID())
            .firstName("John")
            .build();
            
        UserResponse response = mapper.toResponse(user);
        
        assertEquals(user.getFirstName(), response.getFirstName());
    }
}
```

### Tester un Service

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void testGetUser() {
        User user = User.builder().keycloakId("123").build();
        when(userRepository.findByKeycloakId("123"))
            .thenReturn(Optional.of(user));
            
        User result = userService.getUserByKeycloakId("123");
        
        assertEquals(user, result);
    }
}
```

### Tester un Controller

```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    
    @MockBean
    private UserService userService;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testGetProfile() throws Exception {
        UserProfileResponse response = new UserProfileResponse();
        when(userService.getProfile(any()))
            .thenReturn(response);
            
        mockMvc.perform(get("/api/v1/users/me")
                .with(jwt()))
            .andExpect(status().isOk());
    }
}
```

## Déploiement

### Build

```bash
cd ebanking-3.0
./gradlew :user-service:build
```

### Docker

```bash
docker build -f apps/services/user-service/Dockerfile \
  -t user-service:latest .
```

### Configuration

Variables d'environnement:
- `SPRING_DATASOURCE_URL`: URL de la BD
- `SPRING_DATASOURCE_USERNAME`: Username BD
- `SPRING_DATASOURCE_PASSWORD`: Password BD
- `KEYCLOAK_AUTH_SERVER_URL`: URL Keycloak
- `KEYCLOAK_CLIENT_SECRET`: Secret client Keycloak

## Checklist avant commit

- [ ] Code compilé sans erreurs
- [ ] Tests passent (`./gradlew test`)
- [ ] JavaDoc complète pour les méthodes publiques
- [ ] Pas de commentaires TODO oubliés
- [ ] Pas de System.out.println() ou printStackTrace()
- [ ] Validation d'entrée appropriée
- [ ] Gestion d'erreur appropriée
- [ ] Transactions (@Transactional) où nécessaire
- [ ] Code formaté correctement
- [ ] Pas d'imports inutilisés

## Ressources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [Jakarta Persistence](https://jakarta.ee/specifications/persistence/3.1/)
- [MapStruct](https://mapstruct.org/)
- [OAuth2 Resource Server](https://spring.io/guides/tutorials/spring-security-and-oauth2/)

