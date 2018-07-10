package com.auth.service;

import org.springframework.stereotype.Service;

import com.auth.domain.User;

@Service
public class AuthService {
	
	public User findUser() {
		return User.builder()
				.userName("john")
				.userPassword("doe")
				.build();
	}
}
