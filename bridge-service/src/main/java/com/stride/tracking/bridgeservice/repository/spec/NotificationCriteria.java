package com.stride.tracking.bridgeservice.repository.spec;

import org.springframework.data.mongodb.core.query.Criteria;

public class NotificationCriteria {
    private final Criteria criteria = new Criteria();

    public NotificationCriteria haveUserId(String userId) {
        if (userId != null) {
            criteria.and("userId").is(userId);
        }
        return this;
    }

    public NotificationCriteria haveSeen(Boolean seen) {
        if (seen != null) {
            criteria.and("seen").is(seen);
        }
        return this;
    }

    public Criteria build() {
        return criteria;
    }
}
