package com.stride.tracking.bridgeservice.model;

import com.stride.tracking.bridgeservice.persistence.BaseEntity;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "fcm_tokens")
public class FCMToken extends BaseEntity {
    private String token;
    private String userId;
}
