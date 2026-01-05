package com.ebanking.user.api.mapper;

import com.ebanking.shared.dto.UserRequest;
import com.ebanking.shared.dto.UserResponse;
import com.ebanking.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** User Mapper - Convertit les entités User de domaine vers les DTOs de réponse */
@Mapper(componentModel = "spring")
public interface UserMapper {

  /** Convertit un UserRequest en entité User */
  @Mapping(target = "status", constant = "PENDING_REVIEW")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "keycloakId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "kycVerifications", ignore = true) // ← Changé ici
  @Mapping(target = "consents", ignore = true)
  @Mapping(target = "addressLine1", ignore = true)
  @Mapping(target = "addressLine2", ignore = true)
  @Mapping(target = "city", ignore = true)
  @Mapping(target = "postalCode", ignore = true)
  @Mapping(target = "country", ignore = true)
  User toEntity(UserRequest request);

  /**
   * Convertit une entité User en UserResponse
   *
   * <p>Le kycStatus vient maintenant de la KYC la plus récente (getCurrentKycVerification)
   */
  @Mapping(
      target = "id",
      expression = "java(user.getId() != null ? user.getId().toString() : null)")
  @Mapping(target = "status", expression = "java(user.getStatus().name())")
  @Mapping(
      target = "kycStatus",
      expression =
          "java(user.getCurrentKycVerification() != null ? user.getCurrentKycVerification().getStatus().name() : null)")
  UserResponse toResponse(User user);
}
