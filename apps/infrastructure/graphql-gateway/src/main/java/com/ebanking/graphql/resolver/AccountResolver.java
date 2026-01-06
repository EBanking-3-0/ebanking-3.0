package com.ebanking.graphql.resolver;

import com.ebanking.graphql.client.AccountClient;
import com.ebanking.graphql.model.AccountDTO;
import com.ebanking.graphql.model.CreateAccountInput;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AccountResolver {

  private final AccountClient accountClient;

  @QueryMapping
  public List<AccountDTO> myAccounts() {
    return accountClient.getMyAccounts();
  }

  @MutationMapping
  public AccountDTO createAccount(@Argument CreateAccountInput input) {
    return accountClient.createAccount(input);
  }
}
