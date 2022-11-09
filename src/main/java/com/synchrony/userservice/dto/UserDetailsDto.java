package com.synchrony.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsDto {
	private String firstName;
	private String lastName;
	private String emailId;
	private String mobileNumber;
	private String password;
}
