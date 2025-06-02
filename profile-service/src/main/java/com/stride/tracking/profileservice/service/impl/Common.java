package com.stride.tracking.profileservice.service.impl;

import com.stride.tracking.commons.exception.StrideException;
import com.stride.tracking.profileservice.constant.Message;
import com.stride.tracking.profileservice.model.User;
import com.stride.tracking.profileservice.repository.UserRepository;
import org.springframework.http.HttpStatus;

public class Common {
    private Common() {
    }

    public static User findUserById(String userId, UserRepository userRepository) {
        return userRepository.findById(userId).orElseThrow(
                () -> new StrideException(HttpStatus.BAD_REQUEST, Message.USER_NOT_EXIST)
        );
    }
}
