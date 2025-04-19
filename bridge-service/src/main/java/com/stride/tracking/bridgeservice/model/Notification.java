package com.stride.tracking.bridgeservice.model;

import com.stride.tracking.bridgeservice.persistence.BaseEntity;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification extends BaseEntity {

    private String userId;
    private String title;
    private String body;

    private boolean seen;
}
