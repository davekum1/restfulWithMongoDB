package com.auth.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth.config.RequestParameter;
import com.auth.domain.User;
import com.auth.service.AuthService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {
    @Autowired
    private AuthService authService;

    @RequestMapping(
        value = "/login",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ApiOperation(value = "login", notes = "authenticate users")
    @ApiImplicitParams({
        @ApiImplicitParam(
            name = RequestParameter.ACCEPT_LANGUAGE,
            value = RequestParameter.ACCEPT_LANGUAGE_DESC,
            required = false,
            dataType = "string",
            paramType = "header",
            defaultValue = "en"
        )
    })
    public ResponseEntity<?> login(
        HttpServletRequest request,
        @ApiParam(value = "User name", required = true) @RequestParam("userName") String userName,
        @ApiParam(value = "Password", required = true) @RequestParam("password") String userPassword) {
        final User user = authService.login(userName, userPassword);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}