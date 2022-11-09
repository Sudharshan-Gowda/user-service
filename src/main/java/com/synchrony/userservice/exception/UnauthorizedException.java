package com.synchrony.userservice.exception;

@SuppressWarnings("serial")
public class UnauthorizedException extends RuntimeException {
	public UnauthorizedException(String message) {
		super(message);
	}
}
