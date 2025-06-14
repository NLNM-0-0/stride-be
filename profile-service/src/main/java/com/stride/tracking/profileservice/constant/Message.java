package com.stride.tracking.profileservice.constant;

public class Message {
    private Message() {}
    public static final String USER_NOT_EXIST = "User does not exist in the system";
    public static final String JUST_CAN_UPDATE_HEART_RATE_ZONE_ONE_WAY =
            "You can update heart rate zones either via max heart rate or custom zones, but not both simultaneously";
    public static final String MUST_HAVE_ENOUGH_FIVE_HEART_RATE_ZONE = "You must provide exactly five heart rate zones";

    public static final String CAN_NOT_UPDATE_YOURSELF = "You can't update yourself";

    public static final String UPDATE_USER_IDENTITY_FAILED = "Update user identity failed";
    public static final String CREATE_USER_IDENTITY_FAILED = "Create user identity failed";
}
