package com.stride.tracking.identityservice.repository;

import com.stride.tracking.identityservice.model.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {
    boolean existsByToken(String token);
    Optional<AuthToken> findByToken(String token);
}
