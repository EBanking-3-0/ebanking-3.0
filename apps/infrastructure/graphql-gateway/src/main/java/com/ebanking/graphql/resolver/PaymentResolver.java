package com.ebanking.graphql.resolver;

import com.ebanking.graphql.client.PaymentClient;
import com.ebanking.graphql.model.PaymentRequest;
import com.ebanking.graphql.model.PaymentResponse;
import com.ebanking.graphql.model.ScaVerificationRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class PaymentResolver {

  private final PaymentClient paymentClient;

  @MutationMapping
  public PaymentResponse initiateInternalTransfer(@Argument PaymentRequest input) {
    return paymentClient.createInternalTransfer(input);
  }

  @MutationMapping
  public PaymentResponse initiateSepaTransfer(@Argument PaymentRequest input) {
    return paymentClient.createSepaTransfer(input);
  }

  @MutationMapping
  public PaymentResponse initiateInstantTransfer(@Argument PaymentRequest input) {
    return paymentClient.createInstantTransfer(input);
  }

  @MutationMapping
  public PaymentResponse initiateMobileRecharge(@Argument PaymentRequest input) {
    return paymentClient.createMobileRecharge(input);
  }

  @MutationMapping
  public PaymentResponse authorizePayment(@Argument Long paymentId, @Argument String otpCode) {
    return paymentClient.authorizePayment(paymentId, new ScaVerificationRequest(otpCode));
  }

  @QueryMapping
  public List<PaymentResponse> myPayments() {
    return paymentClient.getUserPayments();
  }
}
