package com.stride.tracking.profileservice.repository.spec;

import org.springframework.data.mongodb.core.query.Criteria;

public class UserCriteria {
    private final Criteria criteria = new Criteria();

    public UserCriteria doNotHaveId(String id) {
        if (id != null) {
            criteria.and("id").ne(id);
        }
        return this;
    }

    public UserCriteria isAdmin(Boolean isAdmin) {
        if (isAdmin != null) {
            criteria.and("isAdmin").is(isAdmin);
        }
        return this;
    }

    public UserCriteria isBlocked(Boolean isBlocked) {
        if (isBlocked != null) {
            criteria.and("isBlocked").is(isBlocked);
        }
        return this;
    }

    public UserCriteria hasNameOrEmailContains(String key) {
        if (key != null && !key.isBlank()) {
            Criteria nameCriteria = Criteria.where("name").regex(key, "i");
            Criteria emailCriteria = Criteria.where("email").regex(key, "i");
            criteria.orOperator(nameCriteria, emailCriteria);
        }
        return this;
    }


    public Criteria build() {
        return criteria;
    }
}
