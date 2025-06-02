package com.stride.tracking.metricservice.constant;

public class Message {
    private Message() {
    }

    public static final String SYNC_SPORT_CACHE_FAILED = "Failed to sync sport cache";

    public static final String TRAINING_LOG_FILTER_VALIDATE = "Both fromDate and toDate must be provided together or left blank to use defaults";
    public static final String CAN_NOT_FIND_USER_ID = "Can not find user id";

    public static final String SPORT_IS_NOT_EXISTED = "Sport is not existed";

    public static final String CATEGORY_IS_NOT_EXISTED = "Category is not existed";
}
