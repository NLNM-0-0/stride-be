package com.stride.tracking.coreservice.repository.specs;

import com.stride.tracking.coreservice.model.Route;
import org.springframework.data.jpa.domain.Specification;

public class RouteSpecs {
    private RouteSpecs() {
    }

    public static Specification<Route> isHaveUserId(boolean isHaveUserId) {
        String userIdKey = "userId";

        return (root, query, cb) -> {
            if (isHaveUserId) {
                return cb.isNotNull(root.get(userIdKey));
            } else {
                return cb.isNull(root.get(userIdKey));
            }
        };
    }

    public static Specification<Route> hasUserId(String userId) {
        return (root, query, cb) -> cb.equal(
                root.get("userId"),
                userId
        );
    }

    public static Specification<Route> hasSportId(String sportId) {
        return (root, query, cb) -> cb.equal(
                root.get("sport").get("id"),
                sportId
        );
    }

    public static Specification<Route> hasMinDistance(Integer minDistance) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(
                root.get("totalDistance"),
                minDistance
        );
    }
}