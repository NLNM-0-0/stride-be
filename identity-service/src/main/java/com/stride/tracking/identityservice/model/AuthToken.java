package com.stride.tracking.identityservice.model;

import com.stride.tracking.identityservice.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(
        name = "auth_tokens"
)
public class AuthToken extends BaseEntity {

    @Column(length = 512)
    private String token;

    private Date expiryTime;
}
