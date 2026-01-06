package com.ebanking.user.api.controller;

import com.ebanking.shared.dto.UserProfileResponse;
import com.ebanking.user.api.mapper.UserProfileMapper;
import com.ebanking.user.application.service.UserService;
import com.ebanking.user.domain.model.User;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller - Gère les endpoints liés à la gestion des utilisateurs
 *
 * <p>Endpoints : - GET /api/v1/users/me : Récupérer le profil de l'utilisateur actuel - GET
 * /api/v1/users/{userId} : Récupérer les détails d'un utilisateur (id) - DELETE /api/v1/users/me :
 * Supprimer le compte de l'utilisateur actuel
 *
 * <p>Notes: - L'utilisateur n'est créé que lors de la soumission du KYC - Le statut utilisateur
 * varie entre PENDING_REVIEW, ACTIVE et REJECTED
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;
  private final UserProfileMapper userProfileMapper;

  /**
   * Récupérer le profil de l'utilisateur actuel
   *
   * @param authentication Authentication Spring Security contenant le JWT Keycloak
   * @return 200 OK avec le profil utilisateur 404 Not Found si l'utilisateur n'existe pas (pas
   *     encore soumis de KYC)
   */
  @GetMapping("/me")
  public ResponseEntity<UserProfileResponse> getCurrentUserProfile(Authentication authentication) {
    System.out.println("DEBUG: getCurrentUserProfile called");
    String keycloakId = userService.getKeycloakIdFromJwt(authentication);
    System.out.println("DEBUG: keycloakId extracted: " + keycloakId);

    User user = userService.getUserByKeycloakIdOptional(keycloakId);
    if (user == null) {
      System.out.println("DEBUG: User not found, creating new user from token");
      user = userService.createUserFromToken(authentication);
    }

    System.out.println("DEBUG: User found/created: " + user.getId());
    UserProfileResponse response = userProfileMapper.toResponse(user);
    return ResponseEntity.ok(response);
  }

  /**
   * Récupérer les détails d'un utilisateur par son UUID
   *
   * <p>Accessible uniquement aux administrateurs ou au propriétaire du compte
   *
   * @param userId UUID de l'utilisateur
   * @return 200 OK avec les détails utilisateur 404 Not Found si l'utilisateur n'existe pas
   */
  @GetMapping("/{userId}")
  public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String userId) {
    // TODO : Implémenter la logique de récupération d'utilisateur par ID
    // Ajouter les contrôles de sécurité (admin ou propriétaire)
    return ResponseEntity.notFound().build();
  }

  /**
   * Endpoint de test pour vérifier que le service fonctionne
   *
   * @return Message de confirmation
   */
  @GetMapping("/test")
  public String testEndpoint() {
    return "User service is working correctly!";
  }

  /**
   * Get user contact information by userId (for notification service)
   *
   * @param userId User UUID as string
   * @return 200 OK with user contact data 404 Not Found if user doesn't exist
   */
  @GetMapping("/{userId}/contact")
  public ResponseEntity<?> getUserContact(@PathVariable String userId) {
    try {
      User user = userService.getUserById(UUID.fromString(userId));
      if (user == null) {
        return ResponseEntity.notFound().build();
      }

      // Return contact information for notification service
      Map<String, Object> contact = new HashMap<>();
      contact.put("userId", user.getId());
      contact.put("email", user.getEmail());
      contact.put("phoneNumber", user.getPhone());
      contact.put("firstName", user.getFirstName());
      contact.put("lastName", user.getLastName());
      contact.put("preferredLanguage", user.getPreferredLanguage());

      return ResponseEntity.ok(contact);
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Check if user exists (for notification service)
   *
   * @param userId User UUID as string
   * @return 200 OK with boolean result
   */
  @GetMapping("/{userId}/exists")
  public ResponseEntity<Boolean> userExists(@PathVariable String userId) {
    try {
      User user = userService.getUserById(UUID.fromString(userId));
      return ResponseEntity.ok(user != null);
    } catch (Exception e) {
      return ResponseEntity.ok(false);
    }
  }

  /**
   * Supprimer le compte de l'utilisateur actuel
   *
   * <p>IMPORTANT: Cette opération est irréversible et supprimera tous les données associées
   *
   * @param authentication Authentication Spring Security contenant le JWT Keycloak
   * @return 204 No Content si suppression réussie 404 Not Found si l'utilisateur n'existe pas 403
   *     Forbidden si accès non autorisé
   */
  @DeleteMapping("/me")
  public ResponseEntity<Void> deleteCurrentUserProfile(Authentication authentication) {
    // TODO : Implémenter la logique de suppression
    // - Supprimer les vérifications KYC
    // - Supprimer les consentements GDPR
    // - Anonymiser les données sensibles
    // - Publier un événement Kafka pour les autres services
    return ResponseEntity.noContent().build();
  }

  /**
   * Endpoint de debug pour vérifier les détails de l'authentification
   *
   * @param authentication Authentication Spring Security
   * @return Map contenant les détails de l'authentification
   */
  @GetMapping("/debug-auth")
  public ResponseEntity<Map<String, Object>> debugAuth(Authentication authentication) {
    Map<String, Object> debugInfo = new HashMap<>();
    if (authentication != null) {
      debugInfo.put("name", authentication.getName());
      debugInfo.put("authorities", authentication.getAuthorities());
      debugInfo.put("principal", authentication.getPrincipal());
      debugInfo.put("details", authentication.getDetails());
      debugInfo.put("isAuthenticated", authentication.isAuthenticated());
      debugInfo.put("class", authentication.getClass().getName());
    } else {
      debugInfo.put("message", "No authentication found");
    }
    return ResponseEntity.ok(debugInfo);
  }
}
