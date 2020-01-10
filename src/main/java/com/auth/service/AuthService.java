package com.auth.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.auth.config.ErrorMessageWrapper;
import com.auth.config.PasswordChecker;
import com.auth.domain.UserRequestData;
import com.auth.domain.UserResponseData;
import com.auth.entity.PasswordHistory;
import com.auth.entity.User;
import com.auth.exception.EmailAlreadyInUseException;
import com.auth.exception.LoginAlreadyInUseException;
import com.auth.repository.UserRepository;
import com.auth.utils.PasswordUtil;

@Service
public class AuthService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordHistoryService passwordHistoryService;
	
	/**
	 * Find user by loginId
	 * @param login
	 * @return user 
	 */
    public User findUserByLogin(String login) {
        User user = userRepository.findUserByLogin(login);
        return user;
    }
    
    /**
     * Create new user and save to DB
     * @param userRequestData
     * @return user
     */
    public User createUser(UserRequestData userRequestData) {
        User user = this.mapToNewUser(userRequestData);
        if ("".equals(user.getEmail())) {
            user.setEmail(null); // Don't allow empty string to be saved for an email address
        }
        User savedUser = this.userRepository.save(user);
        return savedUser;
    }

    /**
     * Delete user from DB - hard delete
     * @param login
     * @return user
     */
    public User deleteUserByLogin(String login) {
        User user = this.userRepository.findUserByLogin(login);
        this.userRepository.delete(user);
        return user;
    }

    /**
     * Map from UserRequestData to User entity object
     * @param userRequestData
     * @return
     */
    private User mapToNewUser(UserRequestData userRequestData) {
        ModelMapper modelMapper = new ModelMapper();
        User user = modelMapper.map(userRequestData, User.class);
        user.setFailedLoginAttempts(0);
        user.setPasswordExpirationDate(LocalDateTime.now());
        user.setLocked(false);
        return user;
    }
 

    /*
    public void assertUserLoginNotInUse(String login) {
        if (!StringUtils.isEmpty(login)) {
            try {
                User userbyLogin = userRepository.findUserByLogin(login);
                if (userbyLogin != null) {
                    throw new LoginAlreadyInUseException();
                }
            } catch (Exception e) {
                // since login doesnt exist yet, allow this to continue
            }
        }
    }
    
    public void assertUserEmailNotInUse(String email) {
        if (!StringUtils.isEmpty(email)) {
            try {
                User userbyEmail = userRepository.findUserByEmailIgnoreCase(email);
                if (userbyEmail != null) {
                    throw new EmailAlreadyInUseException();
                }
            } catch (Exception e) {
            	 // since login doesnt exist yet, allow this to continue
            }
        }
    }
 */   
    public int checkLoginAttempt(User user, int maxLoginAttempts) {
        if (maxLoginAttempts <= 0) {
            throw new IllegalArgumentException("Parameter 'maxLoginAttempts' cannot be less than or equal to zero");
        }

        user.incrementFailedLoginAttempts();

        // Determine how many login attempts are left and return the correct response
        int loginAttemptsLeft = maxLoginAttempts - user.getFailedLoginAttempts();
        if (loginAttemptsLeft <= 0) { 
            user.setLocked(true);
        }

        this.userRepository.saveAndFlush(user);
        return loginAttemptsLeft;
    }
    
    public void generatePasswordResetHash(User user, int resetPasswordExpirationHours) {
        if (resetPasswordExpirationHours <= 0) {
            throw new IllegalArgumentException("Parameter 'resetPasswordExpirationHours' cannot be less than or equal to 0");
        }

        SecureRandom secureRandom = new SecureRandom();
        Long nextLong = secureRandom.nextLong();
        String passwordResetHash = PasswordUtil.PASSWORD_ENCODER.encode(nextLong.toString());
        String[] splitString = passwordResetHash.split("\\$"); 
        passwordResetHash = splitString[3].replaceAll("\\/", "");
        user.setPasswordResetHash(passwordResetHash);
        user.setPasswordResetHashExpires(LocalDateTime.now().plusHours(resetPasswordExpirationHours));
        this.userRepository.saveAndFlush(user);
    }
    
    public boolean checkLocked(User user) {
         if (user.getLocked()) {
            return true;
        }
        return false;
    }   
    
    public boolean checkPasswordExpired(User user) {
        PasswordUtil authenticator = new PasswordUtil(user);
        if (!authenticator.isPasswordExpired()) {
            // This User should have already been authenticated. Reset failed login attempts
            user.setFailedLoginAttempts(0);
            this.userRepository.saveAndFlush(user);
            return false;
        }

        return true;
    }
    
    public UserResponseData mapToUserResponseData(User user) {
        UserResponseData userResponseData = new UserResponseData();
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.map(user, userResponseData); 
        return userResponseData;
    }
  
    public void updateUserWithNewPassword(
            User user, PasswordChecker passwordChecker, String newPassword,
            String confirmedPassword,
            ErrorMessageWrapper errorMessage) {

        try {
            boolean isPasswordValid = passwordChecker.isPasswordValid(newPassword, confirmedPassword, user.getEmail(), errorMessage);
            /*
             * If the application configuration for 'passwordRequirements.passwordHistoryReuse' is 0 ensure that
             * the elements per page ('pageSize') is at least 1.
             */
            int pageSize = passwordChecker.getPasswordHistoryReuse() > 0 ? passwordChecker.getPasswordHistoryReuse() : 1;
            PageRequest pageRequest = new PageRequest(0, pageSize, Sort.Direction.DESC, "dateAdded");
            Page<PasswordHistory> pageOne = this.passwordHistoryService.findAllByUuid(pageRequest, user.getUuid());
            List<PasswordHistory> passwordHistories = pageOne.getContent();
            // Ensure the new password is not in the password history for this User
            boolean isPasswordHistoryValid = passwordChecker.isPasswordHistoryValid(user, newPassword, passwordHistories, errorMessage);

            if (isPasswordValid && isPasswordHistoryValid) {
                // If all is good, reset the necessary fields on the User.
                user.setFailedLoginAttempts(0);
                user.setPasswordResetHash(null);
                user.setPasswordResetHashExpires(null);
                user.setLocked(false);

                // Update the User with the new password
                user.setPasswordHash(PasswordUtil.PASSWORD_ENCODER.encode(newPassword));

                // Update the User's password expiration.
                int passwordExpirationDays = passwordChecker.getPasswordExpirationDays();
                LocalDateTime passwordExpiration = LocalDateTime.now().plusDays(passwordExpirationDays);
                user.setPasswordExpirationDate(passwordExpiration);

                /*
                 * If all is successful, save the updated information to the User table,
                 * create a new PasswordHistory entry for the user, and delete their
                 * unused PasswordHistory entries.
                 */
                this.passwordHistoryService.createNewPasswordHistory(user);
                this.passwordHistoryService.deleteUserPasswordHistory(user, passwordChecker.getPasswordHistoryReuse());
            }
        } finally {
            this.userRepository.saveAndFlush(user);
        }
    }    
}
