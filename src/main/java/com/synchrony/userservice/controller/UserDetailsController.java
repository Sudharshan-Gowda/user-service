package com.synchrony.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synchrony.userservice.dto.UserDetailsDto;
import com.synchrony.userservice.dto.UserLoginRequestDto;
import com.synchrony.userservice.response.AuthenticationResponse;
import com.synchrony.userservice.response.SuccessResponse;
import com.synchrony.userservice.service.UserDetailService;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class UserDetailsController {

	@Autowired
	private UserDetailService userDetailsService;

	@PostMapping("/register/user")
	public ResponseEntity<SuccessResponse> registerUser(@RequestBody UserDetailsDto userDetailsDto) {
		return new ResponseEntity<>(
				new SuccessResponse(false, "Registration Success..!", userDetailsService.registerUser(userDetailsDto)),
				HttpStatus.OK);
	}

	@PostMapping("/authenticate")
	public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody UserLoginRequestDto dto) {
		return new ResponseEntity<>(userDetailsService.createAuthenticationToken(dto), HttpStatus.OK);
	}

}
