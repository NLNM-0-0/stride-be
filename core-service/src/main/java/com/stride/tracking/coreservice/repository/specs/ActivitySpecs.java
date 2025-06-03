package com.stride.tracking.coreservice.repository.specs;

import com.stride.tracking.coreservice.model.Activity;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;

public class ActivitySpecs {
    private ActivitySpecs() {
    }

    public static Specification<Activity> hasUser(String userId) {
        return (root, query, cb)
                -> cb.equal(root.get("userId"), userId);
    }

    public static Specification<Activity> hasName(String name) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Activity> hasSportIds(List<String> sportIds) {
        return (root, query, cb) ->
                root.get("sportId").in(sportIds);
    }

    public static Specification<Activity> hasCreatedAtBetween(Instant min, Instant max) {
        if (min == null || max == null) {
            return null;
        }

        return (root, query, cb) ->
                cb.between(root.get("createdAt"), min, max);
    }

    public static Specification<Activity> hasTotalDistanceBetween(Integer min, Integer max) {
        if (min == null || max == null) {
            return null;
        }

        Double minDouble = min.doubleValue();
        Double maxDouble = max.doubleValue();

        return (root, query, cb) ->
                cb.between(root.get("totalDistance"), minDouble, maxDouble);
    }

    public static Specification<Activity> hasElevationGainBetween(Integer min, Integer max) {
        if (min == null || max == null) {
            return null;
        }

        return (root, query, cb) ->
                cb.between(root.get("elevationGain"), min, max);
    }

    public static Specification<Activity> hasMovingTimeSecondsBetween(Integer min, Integer max) {
        if (min == null || max == null) {
            return null;
        }

        return (root, query, cb) ->
                cb.between(root.get("movingTimeSeconds"), min, max);
    }
}