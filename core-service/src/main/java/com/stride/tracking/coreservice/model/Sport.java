package com.stride.tracking.coreservice.model;

import com.stride.tracking.coreservice.persistence.BaseEntity;
import com.stride.tracking.coreservice.utils.converter.list.concrete.RuleListConverter;
import com.stride.tracking.dto.sport.SportMapType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(
        name = "sports"
)
public class Sport extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private String name;
    private String image;

    @Convert(converter = RuleListConverter.class)
    @Column(columnDefinition = "json")
    @ColumnTransformer(write = "?::json")
    private List<Rule> rules;

    private String color;

    private SportMapType sportMapType;
}
