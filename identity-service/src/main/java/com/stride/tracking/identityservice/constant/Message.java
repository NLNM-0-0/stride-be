package com.stride.tracking.identityservice.constant;

public class Message {
	private Message() {}
	public static final String USER_NOT_LOGIN = "Please log in to use this feature";
	public static final String USER_NOT_CORRECT = "Incorrect login information";
	public static final String USER_EXISTED = "User already exists";
	public static final String USER_NOT_EXIST = "User does not exist in the system";
	public static final String USER_IS_BLOCKED = "User has been blocked";
	public static final String JWT_INVALID = "JWT is invalid";
	public static final String ID_TOKEN_INVALID = "Id token is invalid";
	public static final String VERIFIED_TOKEN_NOT_EXIST = "Verification code does not exist";
	public static final String VERIFIED_TOKEN_NOT_CORRECT = "Verification code is incorrect. Please try again";
	public static final String VERIFIED_TOKEN_EXPIRED = "Verification code has expired. Please try again";
	public static final String VERIFIED_TOKEN_EXCEED_MAX_RETRY = "You have requested the verification code too many times. Please check your email or contact us";
	public static final String PROFILE_CREATE_USER_ERROR = "An error occurred while creating a new user";
	public static final String RESET_PASSWORD_TOKEN_EXCEED_MAX_RETRY = "You have requested to reset your password too many times. Please contact us for support";
	public static final String RESET_PASSWORD_TOKEN_NOT_EXIST = "Reset password token does not exist";
	public static final String RESET_PASSWORD_TOKEN_NOT_CORRECT = "Reset password token is incorrect. Please try again";
	public static final String RESET_PASSWORD_TOKEN_EXPIRED = "Reset password token has expired. Please try again";
}
