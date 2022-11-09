package com.synchrony.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.synchrony.userservice.entity.UserDetails;

@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {
	UserDetails findByEmailIdAndMobileNumber(String emailId, String mobileNumber);

	UserDetails findByMobileNumber(String mobileNumber);

	UserDetails findByEmailId(String userName);

}
