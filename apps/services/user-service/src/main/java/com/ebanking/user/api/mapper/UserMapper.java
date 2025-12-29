package com.ebanking.user.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.ebanking.user.domain.model.User;
import com.ebanking.shared.dto.UserRequest;
import com.ebanking.shared.dto.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "status", expression = "java(User.UserStatus.ACTIVE)")
    @Mapping(target = "kycStatus", expression = "java(User.KycStatus.PENDING)")
    User toEntity(UserRequest request);

    @Mapping(target = "status", expression = "java(user.getStatus().name())")
    @Mapping(target = "kycStatus", expression = "java(user.getKycStatus().name())")
    UserResponse toResponse(User user);
}

