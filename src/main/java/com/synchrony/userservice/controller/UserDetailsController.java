package com.synchrony.userservice.controller;

import static com.synchrony.userservice.common.UserDetailsConstant.DEBUG;
import static com.synchrony.userservice.common.UserDetailsConstant.GET_IMAGE_SUCCESS;
import static com.synchrony.userservice.common.UserDetailsConstant.GET_USER_SUCCESS;
import static com.synchrony.userservice.common.UserDetailsConstant.REGISTRATION_SUCCESS;
import static com.synchrony.userservice.common.UserDetailsConstant.UPLOAD_IMAGE_SUCCESS;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.synchrony.userservice.dto.UserDetailsDto;
import com.synchrony.userservice.dto.UserLoginRequestDto;
import com.synchrony.userservice.response.AuthenticationResponse;
import com.synchrony.userservice.response.SuccessResponse;
import com.synchrony.userservice.service.UserDetailService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
@Slf4j
public class UserDetailsController {

	@Autowired
	private UserDetailService userDetailsService;

	@PostMapping("/register/user")
	public ResponseEntity<SuccessResponse> registerUser(@RequestBody UserDetailsDto userDetailsDto) {
		log.debug(DEBUG, userDetailsDto);
		return new ResponseEntity<>(
				new SuccessResponse(false, REGISTRATION_SUCCESS, userDetailsService.registerUser(userDetailsDto)),
				HttpStatus.OK);
	}

	@PostMapping("/authenticate")
	public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody UserLoginRequestDto dto) {
		log.debug(DEBUG);
		return new ResponseEntity<>(userDetailsService.createAuthenticationToken(dto), HttpStatus.OK);
	}

	@PreAuthorize("hasAuthority('USER')")
	@PostMapping("/user/image")
	public ResponseEntity<SuccessResponse> addUserImages(@RequestParam("files") List<MultipartFile> files,
			@RequestParam("userId") long userId) throws IOException {
		log.debug(DEBUG, userId);

		return new ResponseEntity<>(
				new SuccessResponse(false, UPLOAD_IMAGE_SUCCESS, userDetailsService.uploadImages(userId, files)),
				HttpStatus.OK);
	}

	@PreAuthorize("hasAuthority('USER')")
	@GetMapping("/user")
	public ResponseEntity<SuccessResponse> getUserInfo(@RequestParam("userId") long userId) {
		log.debug(DEBUG, userId);
		return new ResponseEntity<>(
				new SuccessResponse(false, GET_USER_SUCCESS, userDetailsService.getUserInfo(userId)), HttpStatus.OK);
	}

	@PreAuthorize("hasAuthority('USER')")
	@GetMapping("/user/image")
	public ResponseEntity<SuccessResponse> getUserImage(@RequestParam("id") String id)
			throws IOException, InterruptedException {
		log.debug(DEBUG);
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		return new ResponseEntity<>(new SuccessResponse(false, GET_IMAGE_SUCCESS, userDetailsService.getImage(id)),
				HttpStatus.OK);
	}
}
