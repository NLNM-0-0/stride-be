package com.stride.tracking.coreservice.repository.specs;

import com.stride.tracking.coreservice.model.Route;
import jakarta.persistence.criteria.Expression;
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

    public static Specification<Route> hasMinDistance(Double minDistance) {
        return (root, query, cb) -> {
            Expression<Double> totalDistance = root.get("totalDistance").as(Double.class);
            Expression<Double> heat = root.get("heat").as(Double.class);

            Expression<Double> distancePerHeat = cb.quot(totalDistance, heat).as(Double.class);

            return cb.and(
                    cb.notEqual(heat, 0.0),
                    cb.greaterThanOrEqualTo(distancePerHeat, cb.literal(minDistance))
            );
        };
    }
}