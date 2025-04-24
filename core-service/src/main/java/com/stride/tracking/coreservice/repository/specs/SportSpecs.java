package com.stride.tracking.coreservice.repository.specs;

import com.stride.tracking.coreservice.model.Sport;
import org.springframework.data.jpa.domain.Specification;

public class SportSpecs {
    private SportSpecs() {
    }

    public static Specification<Sport> hasName(String name) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.get("name")),
                "%" + name.toLowerCase() + "%"
        );
    }

    public static Specification<Sport> hasCategory(String sportId) {
        return (root, query, cb)
                -> cb.equal(root.get("category").get("id"), sportId);
    }
}