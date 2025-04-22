package com.stride.tracking.coreservice.model;

import com.stride.tracking.coreservice.persistence.BaseEntity;
import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(
        name = "categories"
)
public class Category extends BaseEntity {
    private String name;
}
