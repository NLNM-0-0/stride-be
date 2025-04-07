package com.stride.tracking.identityservice.exception;

public class AuthException extends RuntimeException {
  public AuthException(String message, Throwable e) {
    super(message, e);
  }
}
