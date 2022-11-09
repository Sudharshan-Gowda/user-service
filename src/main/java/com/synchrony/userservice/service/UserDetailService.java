package com.synchrony.userservice.service;

import com.synchrony.userservice.dto.UserDetailsDto;
import com.synchrony.userservice.dto.UserLoginRequestDto;
import com.synchrony.userservice.entity.UserDetails;
import com.synchrony.userservice.response.AuthenticationResponse;

public interface UserDetailService {

	public UserDetails registerUser(UserDetailsDto userDetailsDto);

	public AuthenticationResponse createAuthenticationToken(UserLoginRequestDto userLoginRequestPojo);

}
