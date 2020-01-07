package com.auth.utils;

import java.time.LocalDateTime;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

import com.auth.entity.User;

public class PasswordUtil {
	 public static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder(10);

	    private User user;
	    
	    public PasswordUtil(User user) {
	    		this.user = user;	    	
	    }

	    public boolean isAuthenticated(final String rawPassword) {
	        if (StringUtils.isEmpty(this.user.getPasswordHash())) {
	            return false;
	        }
	        return this.doPasswordsMatch(rawPassword, this.user.getPasswordHash());
	    }

	    /**
	     * @return True if the password currently expired, false otherwise
	     */
	    public boolean isPasswordExpired() {
	        LocalDateTime now = LocalDateTime.now();
	        LocalDateTime passwordExpirationDate = this.user.getPasswordExpirationDate();
	        return now.isEqual(passwordExpirationDate) || now.isAfter(passwordExpirationDate);
	    }

	    public boolean isPasswordResetHashValid(String passwordResetHash) {
	        LocalDateTime passwordResetHashExpiration = this.user.getPasswordResetHashExpires();

	        if (user.getPasswordResetHash() == null || passwordResetHashExpiration == null) {
	            return false;
	        }

	        return this.user.getPasswordResetHash().equals(passwordResetHash) && LocalDateTime.now().isBefore(passwordResetHashExpiration);
	    }

	    public boolean doPasswordsMatch(String rawPassword, String passwordHash) {
	        String newPasswordHash = passwordHash;
	        if (passwordHash.matches("^\\$2y.*")) {
	            newPasswordHash = "$2a" + passwordHash.substring(3);
	        }

	        return PasswordUtil.PASSWORD_ENCODER.matches(rawPassword, newPasswordHash);
	    }
}
