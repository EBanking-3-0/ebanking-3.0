package com.ebanking.user.domain.repository;

import com.ebanking.user.domain.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);

  Optional<User> findByKeycloakId(String keycloakId);

  Optional<User> findUserById(String id);
}
