package com.stride.tracking.identityservice.repository;

import com.stride.tracking.identityservice.model.ResetPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, String> {
    Optional<ResetPasswordToken> findByUserIdentity_Id(String userIdentityId);
}
