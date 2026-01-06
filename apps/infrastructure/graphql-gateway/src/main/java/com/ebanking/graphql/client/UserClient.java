package com.ebanking.graphql.client;

import com.ebanking.shared.dto.KycRequest;
import com.ebanking.shared.dto.KycResponse;
import com.ebanking.shared.dto.UserProfileResponse;
import com.ebanking.shared.dto.UserRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "user-service")
public interface UserClient {

  @PostMapping("/api/v1/users")
  UserProfileResponse createUser(@RequestBody UserRequest request);

  @GetMapping("/api/v1/users")
  java.util.List<UserProfileResponse> getUsers();

  @GetMapping("/api/v1/users/{id}")
  UserProfileResponse getUser(
      @org.springframework.web.bind.annotation.PathVariable("id") String id);

  @GetMapping("/api/v1/users/me")
  UserProfileResponse getCurrentUserProfile();

  @PostMapping(value = "api/v1/kyc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  KycResponse submitKyc(
      @RequestPart("data") KycRequest kycRequest,
      @RequestPart("cinImage") MultipartFile cinImage,
      @RequestPart("selfieImage") MultipartFile selfieImage);
}
