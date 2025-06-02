package com.stride.tracking.identityservice.repository;

import com.stride.tracking.identityservice.constant.AuthProvider;
import com.stride.tracking.identityservice.model.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, String> {
    Optional<UserIdentity> findByUsername(String username);
    Optional<UserIdentity> findByProviderAndUserId(AuthProvider provider, String userId);
    Optional<UserIdentity> findByProviderAndEmail(AuthProvider provider, String email);
    List<UserIdentity> findAllByUserId(String userId);
}
