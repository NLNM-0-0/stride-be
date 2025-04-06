package com.stride.tracking.notificationservice.persistence;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
public abstract class BaseEntity extends AuditEntity {
    @Id
    private String id;
}

