package com.synchrony.userservice.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.synchrony.userservice.dto.UserDetailsDto;
import com.synchrony.userservice.dto.UserLoginRequestDto;
import com.synchrony.userservice.entity.UserDetails;
import com.synchrony.userservice.response.AuthenticationResponse;

public interface UserDetailService {

	public UserDetails registerUser(UserDetailsDto userDetailsDto);

	public AuthenticationResponse createAuthenticationToken(UserLoginRequestDto userLoginRequestPojo);

	public UserDetails uploadImages(long userId, List<MultipartFile> file) throws IOException;

	public UserDetails getUserInfo(long userId);

	public Object getImage(String id) throws IOException, InterruptedException;

}
