package com.synchrony.userservice.exception;

@SuppressWarnings("serial")
public class WebException extends RuntimeException {
	public WebException(String message) {
		super(message);
	}
}
