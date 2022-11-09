package com.synchrony.userservice.controlleradvice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.synchrony.userservice.exception.InvalidCredentialException;
import com.synchrony.userservice.exception.PasswordNotMatchedException;
import com.synchrony.userservice.exception.UserDetailsException;
import com.synchrony.userservice.exception.UserNameExistException;
import com.synchrony.userservice.exception.WebException;
import com.synchrony.userservice.response.SuccessResponse;

@RestControllerAdvice
public class UserDetailsExceptionHandler {

	@ExceptionHandler(value = UserNameExistException.class)
	public ResponseEntity<SuccessResponse> handleException(UserNameExistException exception) {
		return new ResponseEntity<>(new SuccessResponse(true, exception.getMessage(), null), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = UserDetailsException.class)
	public ResponseEntity<SuccessResponse> handleException(UserDetailsException exception) {
		return new ResponseEntity<>(new SuccessResponse(true, exception.getMessage(), null), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = PasswordNotMatchedException.class)
	public ResponseEntity<SuccessResponse> handleException(PasswordNotMatchedException exception) {
		return new ResponseEntity<>(new SuccessResponse(true, exception.getMessage(), null), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = InvalidCredentialException.class)
	public ResponseEntity<SuccessResponse> handleException(InvalidCredentialException exception) {
		return new ResponseEntity<>(new SuccessResponse(true, exception.getMessage(), null), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = WebException.class)
	public ResponseEntity<SuccessResponse> handleException(WebException exception) {
		return new ResponseEntity<>(new SuccessResponse(true, exception.getMessage(), null), HttpStatus.BAD_REQUEST);
	}

}
