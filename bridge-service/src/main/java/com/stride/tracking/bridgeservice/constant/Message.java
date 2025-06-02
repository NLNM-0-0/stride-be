package com.stride.tracking.bridgeservice.constant;

public class Message {
    private Message() {}

    public static final String FCM_TOKEN_NOT_EXIST = "FCM token not found";

    public static final String FILE_UPLOAD_FAIL = "An error occurred while uploading the file. Please try again later.";
    public static final String FILE_UPLOAD_TOO_LARGE = "The file you are trying to upload is too large. Please choose a smaller file.";

    public static final String NOTIFICATION_NOT_EXISTED = "Notification not found";
    public static final String CAN_NOT_SEE_OTHER_USER_NOTIFICATIONS = "Can't see other user notifications";
}
