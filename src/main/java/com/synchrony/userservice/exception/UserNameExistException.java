package com.synchrony.userservice.exception;

@SuppressWarnings("serial")
public class UserNameExistException extends RuntimeException {
	public UserNameExistException(String message) {
		super(message);
	}
}
