package com.synchrony.userservice.exception;

@SuppressWarnings("serial")
public class InvalidCredentialException extends RuntimeException {
	public InvalidCredentialException(String message) {
		super(message);
	}

}
