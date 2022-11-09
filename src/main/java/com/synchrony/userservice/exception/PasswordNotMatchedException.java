package com.synchrony.userservice.exception;

@SuppressWarnings("serial")
public class PasswordNotMatchedException extends RuntimeException {

	public PasswordNotMatchedException(String message) {
		super(message);
	}
}
