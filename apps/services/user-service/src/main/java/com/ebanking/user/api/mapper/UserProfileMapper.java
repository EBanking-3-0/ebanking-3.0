package com.ebanking.user.api.mapper;

import com.ebanking.shared.dto.UserProfileResponse;
import com.ebanking.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** User Profile Mapper - Convertit les entités User vers le DTO de profil utilisateur */
@Mapper(componentModel = "spring")
public interface UserProfileMapper {

  /**
   * Convertit une entité User en réponse de profil utilisateur
   *
   * <p>Mappages: - id: Convertir UUID en String - status: Convertir UserStatus en String -
   * kycStatus: Prendre le statut de la KYC la plus récente (via getCurrentKycVerification)
   */
  @Mapping(
      target = "id",
      expression = "java(user.getId() != null ? user.getId().toString() : null)")
  @Mapping(target = "status", expression = "java(user.getStatus().name())")
  @Mapping(
      target = "kycStatus",
      expression =
          "java(user.getCurrentKycVerification() != null ? user.getCurrentKycVerification().getStatus().name() : null)")
  UserProfileResponse toResponse(User user);
}
