package com.ebanking.graphql.client;

import com.ebanking.shared.dto.UserProfileResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "user-service")
public interface UserClient {

    @GetMapping("api/v1/users/me")
    UserProfileResponse getCurrentUserProfile();
}
