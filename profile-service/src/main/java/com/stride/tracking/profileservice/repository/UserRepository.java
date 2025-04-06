package com.stride.tracking.profileservice.repository;

import com.stride.tracking.profileservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
}
