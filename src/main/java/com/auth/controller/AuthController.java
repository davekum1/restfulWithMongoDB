package com.auth.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth.config.ErrorMessageWrapper;
import com.auth.config.MessageResource;
import com.auth.config.PasswordChecker;
import com.auth.config.RequestParamConstant;
import com.auth.config.RequestParameter;
import com.auth.config.ResponseCode;
import com.auth.config.ResponseCodeConfiguration;
import com.auth.config.ResponseMessage;
import com.auth.domain.UserRequestData;
import com.auth.domain.UserResponseData;
import com.auth.entity.User;
import com.auth.service.AuthService;
import com.auth.utils.PasswordUtil;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api/authentication")
@Validated
public class AuthController {
    @Autowired
    private AuthService authService;
    
    @Autowired
    private PasswordChecker passwordChecker;

    @Autowired
    private ResponseCodeConfiguration responseCodeConfiguration;

    @Value("${passwordRequirements.resetPasswordExpirationHours}")
    private int resetPasswordExpirationHours;

    @Value("${passwordRequirements.newUserExpirationHours}")
    private int newUserExpirationHours;

    @Value("${application.supportedLocales}")
    private String[] supportedLocales;
    
    @RequestMapping(
            value = "/login",
            method = RequestMethod.POST
        )
        @ApiOperation(value = "authenticate", notes  = "Authenticate to service")
        @ApiImplicitParams({
            @ApiImplicitParam(
                name = RequestParamConstant.ACCEPT_LANGUAGE,
                value = RequestParamConstant.ACCEPT_LANGUAGE_DESC,
                required = false,
                dataType = "string",
                paramType = "header",
                defaultValue = "en"
            )
        })
        public ResponseEntity<?> authenticate(
                HttpServletRequest request,
                @ApiParam(value = RequestParamConstant.LOGIN_DESC, required = true)
                @RequestParam(RequestParamConstant.LOGIN) String login,
                @ApiParam(value = RequestParamConstant.PASSWORD_DESC, required = true)
                @RequestParam(RequestParamConstant.PASSWORD) String password) {
            ErrorMessageWrapper errorMessage = new ErrorMessageWrapper("password");
            User user = authService.findUserByLogin(login);
            if (user == null) {
            		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            PasswordUtil passwordUtil = new PasswordUtil(user);

            // check if account is locked, if yes, then exit
            if (this.authService.checkLocked(user)) {
                ResponseCode responseCode = new ResponseCode(
                        this.responseCodeConfiguration.getAccountLocked(),
                        "accountLocked",
                        this.resetPasswordExpirationHours);
                return new ResponseEntity<>(new ResponseMessage(responseCode, request.getRequestURI()), HttpStatus.UNAUTHORIZED);
            }

            if (!passwordUtil.isAuthenticated(password)) {
                return this.buildLoginAttemptResponse(user, request);
            }

            // If here authentication succeeded and the account was not locked, then check whether or not password is not expired
            if (this.authService.checkPasswordExpired(user)) {
                ResponseCode responseCode = new ResponseCode(
                        this.responseCodeConfiguration.getPasswordExpired(),
                        "passwordExpired",
                        this.resetPasswordExpirationHours);
                return this.buildResponse(HttpStatus.UNAUTHORIZED, responseCode, request);
            }
            else {
                // Update the UserResponseData with the latest User settings that were updated as part of this method call
                UserResponseData userResponseData = authService.mapToUserResponseData(user);
                return new ResponseEntity<>(userResponseData, HttpStatus.OK);
            }

        }

    @RequestMapping(
            value = "/{login:.+}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
        )
        @ApiOperation(value = "findUserByLogin", notes = "Retrive user detail information")
        @ApiImplicitParams({
            @ApiImplicitParam(
                name = RequestParamConstant.ACCEPT_LANGUAGE,
                value = RequestParamConstant.ACCEPT_LANGUAGE_DESC,
                required = false,
                dataType = "string",
                paramType = "header",
                defaultValue = "en"
            )
        })
        public ResponseEntity<?> findUserByLogin(
                HttpServletRequest request,
                @ApiParam(value = RequestParamConstant.LOGIN_DESC, required = true)
                @PathVariable(RequestParamConstant.LOGIN) String login) {
            User user = authService.findUserByLogin(login);
            if (user == null) {            	
            	 return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            UserResponseData userResponseData = authService.mapToUserResponseData(user);
            return new ResponseEntity<>(userResponseData, HttpStatus.OK);
    }
 
    
    @RequestMapping(
            value = "/",
            method = RequestMethod.POST
        )
        @ApiOperation(value = "createUser", notes = "Create new user in Mednet user table.")
        @ApiImplicitParams({
            @ApiImplicitParam(
                name = RequestParamConstant.ACCEPT_LANGUAGE,
                value = RequestParamConstant.ACCEPT_LANGUAGE_DESC,
                required = false,
                dataType = "string",
                paramType = "header",
                defaultValue = "en"
            )
        })
        public ResponseEntity<?> createUserEndPoint(
                HttpServletRequest request,
                @ApiParam(value = RequestParamConstant.USER_REQUEST_DATA_DESC, required = true)
                @Validated @RequestBody UserRequestData userRequestData) {

            // If here there were no validation errors: save the user and send the response.
            User user = this.authService.createUser(userRequestData); // Login cannot be null or empty
            // generate new hash since password is expired
            this.authService.generatePasswordResetHash(user, this.newUserExpirationHours);
            UserResponseData userResponseData = this.authService.mapToUserResponseData(user);
            return new ResponseEntity<>(userResponseData, HttpStatus.CREATED);
        }
   
 
    
    private ResponseEntity<ResponseMessage> buildLoginAttemptResponse(User user, HttpServletRequest request) {
        if (this.passwordChecker.getAccountLockedAttempts() == 0) {
            // The failed login attempts is turned off
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // Determine how many login attempts are left and return the correct response
        int loginAttemptsLeft = authService.checkLoginAttempt(user, this.passwordChecker.getAccountLockedAttempts());
        // Default response: User has numLoginAttemptsLeft remaining
        ResponseEntity<ResponseMessage> responseEntity = this.buildResponse(
                HttpStatus.UNAUTHORIZED,
                request,
                "authentication.failedLoginAttempt",
                loginAttemptsLeft);
        if (loginAttemptsLeft <= 0) { // Account just became locked
            ResponseCode responseCode = new ResponseCode(
                    this.responseCodeConfiguration.getAccountJustBecameLocked(),
                    "accountJustBecameLocked",
                    this.resetPasswordExpirationHours);
            responseEntity = this.buildResponse(HttpStatus.UNAUTHORIZED, responseCode, request);
            this.authService.generatePasswordResetHash(user, this.resetPasswordExpirationHours);           
        } else if (loginAttemptsLeft == 1) {
            responseEntity = this.buildResponse(HttpStatus.UNAUTHORIZED, request, "authentication.oneMorefailedLoginAttempt");
        }

        return responseEntity;
    }    
    
    @RequestMapping(
            value = "/{login:.+}/changePasswordWithToken",
            method = RequestMethod.POST
        )
        @ApiOperation(value = "changePasswordWithToken", notes = "Resetting password via token.")
        @ApiImplicitParams({
            @ApiImplicitParam(
                name = RequestParamConstant.ACCEPT_LANGUAGE,
                value = RequestParamConstant.ACCEPT_LANGUAGE_DESC,
                required = false,
                dataType = "string",
                paramType = "header",
                defaultValue = "en"
            )
        })
        public ResponseEntity<ResponseMessage> changePasswordWithToken(
                HttpServletRequest request,
                @ApiParam(value = RequestParamConstant.LOGIN_DESC, required = true)
                @PathVariable String login,
                // This will be used to authenticate the user before changing their password
                @ApiParam(value = RequestParamConstant.PASSWORD_RESET_HASH_DESC, required = true)
                @RequestParam(RequestParamConstant.PASSWORD_RESET_HASH) String passwordResetHash,
                // The new password to change to
                @ApiParam(value = RequestParamConstant.NEW_PASSWORD_DESC, required = true)
                @RequestParam(RequestParamConstant.NEW_PASSWORD) String newPassword,
                // The user must confirm the password to change it
                @ApiParam(value = RequestParamConstant.CONFIRMED_PASSWORD_DESC, required = true)
                @RequestParam(RequestParamConstant.CONFIRMED_PASSWORD) String confirmedPassword) {
            ErrorMessageWrapper errorMessage = new ErrorMessageWrapper("password");
            User user = this.authService.findUserByLogin(login);


            PasswordUtil authenticator = new PasswordUtil(user);
            if (authenticator.isPasswordResetHashValid(passwordResetHash)) {
                this.authService.updateUserWithNewPassword(user, this.passwordChecker, newPassword, confirmedPassword, errorMessage);
                // If the password reset hash is valid, always return the OK response with the error messages
                return this.buildResponse(HttpStatus.OK, errorMessage.getErrorMessages(), request);
            }

            return this.buildResponse(HttpStatus.UNAUTHORIZED, errorMessage.getErrorMessages(), request);
        }
   
    
    // Building ResponseEntity
    // -----------------------
    private ResponseEntity<ResponseMessage> buildResponse(HttpStatus httpStatus, HttpServletRequest request, String fieldString, Object... fields) {
        return new ResponseEntity<>(
                new ResponseMessage(httpStatus, MessageResource.getInstance().resolveMessage(fieldString, fields), request.getRequestURI()),
                httpStatus);
    }

    private ResponseEntity<ResponseMessage> buildResponse(HttpStatus httpStatus, Map<String, List<String>> messages, HttpServletRequest request) {
        return new ResponseEntity<>(
                new ResponseMessage(httpStatus, messages, request.getRequestURI()),
                httpStatus);
    }

    private ResponseEntity<ResponseMessage> buildResponse(HttpStatus httpStatus, ResponseCode responseCode, HttpServletRequest request) {
        return new ResponseEntity<>(new ResponseMessage(responseCode, request.getRequestURI()), httpStatus);
    }
    
    //-------------------------
}