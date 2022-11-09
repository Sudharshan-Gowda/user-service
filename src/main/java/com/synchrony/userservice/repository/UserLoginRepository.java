package com.synchrony.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.synchrony.userservice.entity.UserLogin;

@Repository
public interface UserLoginRepository extends JpaRepository<UserLogin, Long> {

	UserLogin findByEmailId(String emailId);

}
