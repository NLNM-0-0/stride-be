package com.stride.tracking.apigateway.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Date;

@AllArgsConstructor
@Getter
@Builder
public class ErrorResponse {
	private Date timestamp;
	private HttpStatus status;
	private String message;
	private String detail;
}
