package com.nebras.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCaseAndActiveTrue(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByRole(Role role);
}
