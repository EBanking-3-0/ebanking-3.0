package com.ebanking.graphql.resolver;

import com.ebanking.graphql.client.UserClient;
import com.ebanking.graphql.model.CreateUserInput;
import com.ebanking.graphql.model.KycRequestInput;
import com.ebanking.shared.dto.KycRequest;
import com.ebanking.shared.dto.KycResponse;
import com.ebanking.shared.dto.UserProfileResponse;
import com.ebanking.shared.dto.UserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class UserResolver {

  private final UserClient userClient;

  @QueryMapping
  public UserProfileResponse me() {
    return userClient.getCurrentUserProfile();
  }

  @QueryMapping
  public java.util.List<UserProfileResponse> users() {
    return userClient.getUsers();
  }

  @QueryMapping
  public UserProfileResponse user(@Argument String id) {
    return userClient.getUser(id);
  }

  @MutationMapping
  public UserProfileResponse createUser(@Argument("input") CreateUserInput input) {
    UserRequest request = new UserRequest();
    request.setEmail(input.getEmail());
    request.setFirstName(input.getFirstName());
    request.setLastName(input.getLastName());
    request.setPhone(input.getPhone());
    return userClient.createUser(request);
  }

  @MutationMapping
  public KycResponse submitKyc(
      @Argument("input") KycRequestInput input,
      @Argument("cinImage") MultipartFile cinImage,
      @Argument("selfieImage") MultipartFile selfieImage) {

    KycRequest kycRequest = new KycRequest();
    kycRequest.setFirstName(input.getFirstName());
    kycRequest.setLastName(input.getLastName());
    kycRequest.setPhone(input.getPhone());
    kycRequest.setAddressLine1(input.getAddressLine1());
    kycRequest.setAddressLine2(input.getAddressLine2());
    kycRequest.setCity(input.getCity());
    kycRequest.setPostalCode(input.getPostalCode());
    kycRequest.setCountry(input.getCountry());
    kycRequest.setCinNumber(input.getCinNumber());
    kycRequest.setGdprConsents(input.getGdprConsents());

    return userClient.submitKyc(kycRequest, cinImage, selfieImage);
  }
}
