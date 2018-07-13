package com.auth.service;

import org.springframework.stereotype.Service;

import com.auth.domain.User;

@Service
public class AuthService {
    public User login(String userName, String userPassword) {
        return User.builder()
                .userName(userName)
                .userPassword(userPassword)
                .build();
    }
}
