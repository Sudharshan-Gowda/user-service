package com.synchrony.userservice.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtUserDetailsService implements UserDetailsService {

	@Autowired
	private com.synchrony.userservice.repository.UserLoginRepository userLoginRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		com.synchrony.userservice.entity.UserLogin findByEmailId = userLoginRepository.findByEmailId(username);
		if (findByEmailId == null) {
			throw new UsernameNotFoundException("User Not Found With " + username);
		} else if (findByEmailId.getPassword() == null) {
			throw new UsernameNotFoundException("Please Set The Password And Try Again");
		}
		List<SimpleGrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_" + findByEmailId.getUserRole()));
		return new org.springframework.security.core.userdetails.User(findByEmailId.getEmailId(),
				findByEmailId.getPassword(), authorities);

	}

}
