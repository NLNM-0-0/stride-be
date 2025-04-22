package com.stride.tracking.identityservice.model;

import com.stride.tracking.identityservice.constant.AuthProvider;
import com.stride.tracking.identityservice.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(
        name = "user_identities"
)
public class UserIdentity extends BaseEntity {

    private String userId;

    @Column(
            name = "username",
            unique = true
    )
    private String username;

    @Column(
            name = "password",
            unique = true
    )
    private String password;

    @Column(
            name = "email",
            unique = true
    )
    private String email;

    @Column(
            name = "provider"
    )
    private AuthProvider provider;

    @Column(
            name = "provider_id"
    )
    private String providerId;

    private boolean isAdmin;

    private boolean isVerified;

    private boolean isBlocked;
}
