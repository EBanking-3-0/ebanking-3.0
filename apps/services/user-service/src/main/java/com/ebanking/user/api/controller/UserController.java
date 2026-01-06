package com.ebanking.user.api.controller;

import com.ebanking.shared.dto.UserProfileResponse;
import com.ebanking.user.api.mapper.UserProfileMapper;
import com.ebanking.user.application.service.UserService;
import com.ebanking.user.domain.model.User;
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
    // Extraire le keycloakId du JWT
    String keycloakId = userService.getKeycloakIdFromJwt(authentication);

    // Récupérer l'utilisateur (sans créer s'il n'existe pas)
    User user = userService.getUserByKeycloakIdOptional(keycloakId);
    if (user == null) {
      return ResponseEntity.notFound().build();
    }

    // Mapper et retourner le profil utilisateur
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
}
