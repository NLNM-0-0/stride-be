package com.stride.tracking.coreservice.repository.specs;

import com.stride.tracking.coreservice.model.Category;
import org.springframework.data.jpa.domain.Specification;

public class CategorySpecs {
    private CategorySpecs() {
    }

    public static Specification<Category> hasName(String name) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.get("name")),
                "%" + name.toLowerCase() + "%"
        );
    }
}