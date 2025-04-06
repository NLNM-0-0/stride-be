package com.stride.tracking.identityservice.model;

import com.stride.tracking.identityservice.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(
        name = "verified_tokens"
)
public class VerifiedToken extends BaseEntity {
    public static final int MAX_RETRY = 5;

    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserIdentity userIdentity;

    private Date expiryTime;

    int retry;
}
