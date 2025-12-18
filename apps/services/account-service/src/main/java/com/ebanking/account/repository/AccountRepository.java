package com.ebanking.account.repository;

import com.ebanking.account.model.Account;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
  Optional<Account> findByAccountNumber(String accountNumber);

  List<Account> findByUserId(Long userId);
}
