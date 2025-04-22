package com.stride.tracking.coreservice.repository.specs;

import com.stride.tracking.coreservice.model.Activity;
import org.springframework.data.jpa.domain.Specification;

public class ActivitySpecs {
    private ActivitySpecs() {
    }

    public static Specification<Activity> hasUser(String userId) {
        return (root, query, cb)
                -> cb.equal(root.get("userId"), userId);
    }
}