package com.stride.tracking.identityservice.repository;

import com.stride.tracking.identityservice.model.VerifiedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerifiedTokenRepository extends JpaRepository<VerifiedToken, String> {
    Optional<VerifiedToken> findByUserIdentity_Id(String userIdentityId);
}
