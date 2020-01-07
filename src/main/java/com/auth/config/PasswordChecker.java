package com.auth.config;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.auth.entity.PasswordHistory;
import com.auth.entity.User;
import com.auth.utils.PasswordUtil;

@Configuration
public class PasswordChecker {
	 public static final String SPECIAL_CHARACTERS = "!\"# $%&'()*+,-./:;<=>?@[\\]^_`{|}~";
	    public static final int NUMBER_REUSED_PASSWORDS = 2;
	    public static final int MAX_PASSWORD_EXPIRATION_DAYS  = 180;
	    public static final int MAX_PASSWORD_HISTORY_REUSE = 10;
	    public static final int MAX_ACCOUNT_LOCKED = 10;
	    public static final int PASSWORD_NEVER_EXPIRES_YEARS = 100;

	    private static final int MIN_PASSWORD_LENGTH  = 1;
	    private static final int MAX_PASSWORD_LENGTH  = 128;
	    private static final int MAX_CONFIGURED_RULES = 4;

	    private String newPassword;
	    private String email;
	    private String confirmedPassword;
	    private int numRulesConfigured;

	    // A value of 0 means that this is turned off
	    @Value("${passwordRequirements.passwordExpirationDays}")
	    @lombok.Getter
	    private int passwordExpirationDays;

	    // A value of 0 means that this is turned off
	    @Value("${passwordRequirements.passwordHistoryReuse}")
	    @lombok.Getter
	    private int passwordHistoryReuse;

	    @Value("${passwordRequirements.accountLockedAttempts}")
	    @lombok.Getter
	    private int accountLockedAttempts;

	    @Value("${passwordRequirements.minPasswordLength}")
	    private int minPasswordLength;

	    @Value("${passwordRequirements.maxPasswordLength}")
	    private int maxPasswordLength;

	    @Value("${passwordRequirements.requiredCharSets.numRulesToMatch}")
	    private int numRulesToMatch;

	    @Value("${passwordRequirements.requiredCharSets.upperCaseRequired}")
	    private boolean upperCaseRequired;

	    @Value("${passwordRequirements.requiredCharSets.lowerCaseRequired}")
	    private boolean lowerCaseRequired;

	    @Value("${passwordRequirements.requiredCharSets.digitRequired}")
	    private boolean digitRequired;

	    @Value("${passwordRequirements.requiredCharSets.specialCharacterRequired}")
	    private boolean specialCharacterRequired;

	    @Value("${passwordRequirements.allowEmailMatch}")
	    private boolean allowEmailMatch;

	    @Value("${passwordRequirements.allowRepeatedChars}")
	    private boolean allowRepeatedChars;

	    public PasswordChecker() {}

	    public PasswordChecker(
	            int minPasswordLength, int maxPasswordLength, int numRulesToMatch, boolean upperCaseRequired,
	            boolean lowerCaseRequired, boolean digitRequired, boolean specialCharacterRequired,
	            boolean allowEmailMatch, boolean allowRepeatedChars, int passwordExpirationDays,
	            int passwordHistoryReuse, int accountLockedAttempts) {
	        this();
	        this.minPasswordLength = minPasswordLength;
	        this.maxPasswordLength = maxPasswordLength;
	        this.numRulesToMatch = numRulesToMatch;
	        this.upperCaseRequired = upperCaseRequired;
	        this.lowerCaseRequired = lowerCaseRequired;
	        this.digitRequired = digitRequired;
	        this.specialCharacterRequired = specialCharacterRequired;
	        this.allowEmailMatch = allowEmailMatch;
	        this.allowRepeatedChars = allowRepeatedChars;
	        this.passwordExpirationDays = passwordExpirationDays;
	        this.passwordHistoryReuse = passwordHistoryReuse;
	        this.accountLockedAttempts = accountLockedAttempts;
	    }

	    /**
	     * Check to see if the password is valid, checking against all configured password rules.
	     */
	    public boolean isPasswordValid(String newPassword, String confirmedPassword, String email, ErrorMessageWrapper errorMessage) {
	        return this.validatePasswords(newPassword, confirmedPassword, email, errorMessage);
	    }

	    /**
	     * Check to see if the password is valid, checking against all configured password rules.
	     */
	    public boolean checkPassword(String newPassword, String email, ErrorMessageWrapper errorMessage) {
	        return this.validatePasswords(newPassword, null, email, errorMessage);
	    }

	    /**
	     * NOTE: This list of PasswordHistory(s) MUST be sorted by date_added DESC.
	     * @return True if there is NO password history for this User.
	     *         True if the last 2 items in the list of PasswordHistory do not match the User->getPasswordResetHash()
	     *         False otherwise.
	     */
	    public boolean isPasswordHistoryValid(User user, String newPassword, List<PasswordHistory> passwordHistories, ErrorMessageWrapper errorMessage) {
	        this.newPassword = newPassword;
	        if (passwordHistories == null || passwordHistories.size() <= 0 || this.passwordHistoryReuse <= 0) {
	            return true;
	        }

	        // Only check the last 2 or a number less than 2
	        int passwordHistoryLength = passwordHistories.size() < this.passwordHistoryReuse ? passwordHistories.size()
	                                                                                         : this.passwordHistoryReuse;
	        for (int i = 0; i < passwordHistoryLength; i++) {
	            PasswordHistory passwordHistory = passwordHistories.get(i);
	            PasswordUtil authenticator = new PasswordUtil(user);
	            if (authenticator.doPasswordsMatch(this.newPassword, passwordHistory.getPasswordHash())) {
	                errorMessage.addErrorMessage("newPassword", "reuse", NUMBER_REUSED_PASSWORDS);
	                return false;
	            }
	        }

	        return true;
	    }

	    /**
	     * Check to see if the password is valid, checking against all configured password rules.
	     */
	    private boolean validatePasswords(String newPassword, String confirmedPassword, String email, ErrorMessageWrapper errorMessage) {
	        this.newPassword = newPassword;
	        this.confirmedPassword = confirmedPassword;
	        if (!StringUtils.isEmpty(email)) {
	            this.email = email; // ePro users don't have an email address
	        }

	        this.initNumRulesConfigured();
	        this.runAssertions();

	        this.validatePrimaryRules(errorMessage);
	        this.validateRequiredCharSetRules(errorMessage);
	        if (this.confirmedPassword != null && !this.newPassword.equals(this.confirmedPassword)) {
	            errorMessage.addErrorMessage("confirmedPassword", "mustMatch");
	        }

	        return errorMessage.getErrorMessages().size() == 0 ? true : false;
	    }

	    /**
	     * Helper method to set how many of the password complexity or character set rules are configured.
	     */
	    private void initNumRulesConfigured() {
	        this.numRulesConfigured = 0;
	        if (this.upperCaseRequired) {
	            this.numRulesConfigured++;
	        }
	        if (this.lowerCaseRequired) {
	            this.numRulesConfigured++;
	        }
	        if (this.digitRequired) {
	            this.numRulesConfigured++;
	        }
	        if (this.specialCharacterRequired) {
	            this.numRulesConfigured++;
	        }
	    }

	    /**
	     * Helper method to verify min/max password length, and any other miscellaneous rules.
	     * @param errorMessage Error message object containing list of errors
	     */
	    private void validatePrimaryRules(ErrorMessageWrapper errorMessage) {
	        if (this.newPassword.length() < this.minPasswordLength) {
	            errorMessage.addErrorMessage("newPassword", "minPasswordLength", this.minPasswordLength, this.maxPasswordLength);
	        }

	        if (this.minPasswordLength > 0 && this.newPassword.length() > this.maxPasswordLength) {
	            errorMessage.addErrorMessage("newPassword", "maxPasswordLength", this.maxPasswordLength);
	        }

	        if (!StringUtils.isEmpty(this.email) && !this.allowEmailMatch && this.newPassword.toLowerCase().contains(this.email.toLowerCase())) {
	            errorMessage.addErrorMessage("newPassword", "allowEmailMatch", this.allowEmailMatch);
	        }

	        if (!this.allowRepeatedChars && this.threeInARow()) {
	            errorMessage.addErrorMessage("newPassword", "allowRepeatedChars", this.allowRepeatedChars);
	        }
	    }

	    /**
	     * Helper method to verify password complexity rules on given character sets.
	     * @param errorMessage Error message object containing list of errors
	     */
	    private void validateRequiredCharSetRules(ErrorMessageWrapper errorMessage) {
	        int numUpperCaseFound = this.newPassword.replaceAll("[^A-Z]", "").length();
	        int numLowerCaseFound = this.newPassword.replaceAll("[^a-z]", "").length();
	        int numDigitsFound = this.newPassword.replaceAll("[^0-9]", "").length();
	        int numSpecialCharactersFound = this.newPassword.replaceAll("[^" + Pattern.quote(PasswordChecker.SPECIAL_CHARACTERS) + "]", "").length();

	        if (this.numRulesToMatch == this.numRulesConfigured) {
	            /*
	             * In the application configuration file the configured 'numRulesToMatch' equals the actual number
	             * of 'requiredCharSets' that are set to 'true'. In this case individual rules are checked and
	             * whether the password is valid is based on the passage of these individual rules.
	             *
	             * In this configuration we are being asked to ensure that there is one upper case character in the password:
	             *   requiredCharSets:
	             *      numRulesToMatch: 1
	             *      upperCaseRequired: true // This is a configured rule
	             *      lowerCaseRequired: false
	             *      digitRequired: false
	             *      specialCharacterRequired: false
	             * In this configuration we are being asked to ensure that there is one upper case character and 1 special character in the password:
	             *   requiredCharSets:
	             *      numRulesToMatch: 2
	             *      upperCaseRequired: true
	             *      lowerCaseRequired: false        // This is a configured rule
	             *      digitRequired: false
	             *      specialCharacterRequired: true  // This is a configured rule
	             */
	            if (this.upperCaseRequired && numUpperCaseFound < 1) {
	                errorMessage.addErrorMessage("newPassword", "upperCaseRequired");
	            }
	            if (this.lowerCaseRequired && numLowerCaseFound < 1) {
	                errorMessage.addErrorMessage("newPassword", "lowerCaseRequired");
	            }
	            if (this.digitRequired && numDigitsFound < 1) {
	                errorMessage.addErrorMessage("newPassword", "digitRequired");
	            }
	            if (this.specialCharacterRequired && numSpecialCharactersFound < 1) {
	                String spaceMsg = MessageResource.getInstance().resolveMessage("password.specialCharacterRequired.space");
	                String spaceNamed = PasswordChecker.SPECIAL_CHARACTERS.replace(" ", spaceMsg);
	                errorMessage.addErrorMessage("newPassword", "specialCharacterRequired", spaceNamed);
	            }
	        } else {
	            /*
	             * In the application configuration file the configured 'numRulesToMatch' does NOT equal
	             * the actual number of 'requiredCharSets' that are set to 'true'.
	             * In this configuration we are being asked to match 3 of the 4 configured rules:
	             *   requiredCharSets:
	             *      numRulesToMatch: 3
	             *      upperCaseRequired: true        // This is a configured rule
	             *      lowerCaseRequired: true        // This is a configured rule
	             *      digitRequired: true            // This is a configured rule
	             *      specialCharacterRequired: true // This is a configured rule
	             * In this configuration we are being asked to match 1 of the 2 configured rules:
	             *   requiredCharSets:
	             *      numRulesToMatch: 1
	             *      upperCaseRequired: true   // This is a configured rule
	             *      lowerCaseRequired: false
	             *      digitRequired: true       // This is a configured rule
	             *      specialCharacterRequired: false
	             */
	            int numberRulesPassed = 0;
	            if (this.upperCaseRequired && numUpperCaseFound >= 1) {
	                numberRulesPassed++;
	            }
	            if (this.lowerCaseRequired && numLowerCaseFound >= 1) {
	                numberRulesPassed++;
	            }
	            if (this.digitRequired && numDigitsFound >= 1) {
	                numberRulesPassed++;
	            }
	            if (this.specialCharacterRequired && numSpecialCharactersFound >= 1) {
	                numberRulesPassed++;
	            }
	            if (numberRulesPassed < this.numRulesToMatch) {
	                String spaceMsg = MessageResource.getInstance().resolveMessage("password.specialCharacterRequired.space");
	                String spaceNamed = PasswordChecker.SPECIAL_CHARACTERS.replace(" ", spaceMsg);
	                errorMessage.addErrorMessage("newPassword", "numRulesMet", spaceNamed);
	            }
	        }
	    }

	    /**
	     * Run any necessary assertions that should be checked prior to inspecting the password
	     * with the password rules.
	     */
	    private void runAssertions() {

	        if (this.numRulesToMatch > MAX_CONFIGURED_RULES) {
	            throw new IllegalStateException("You cannot have 'numRulesToMatch' exceeding the value " + MAX_CONFIGURED_RULES);
	        }

	        if (this.numRulesToMatch < 0) {
	            throw new IllegalStateException("You cannot have 'numRulesToMatch' less than 0");
	        }

	        if (this.numRulesToMatch > this.numRulesConfigured) {
	            throw new IllegalStateException("You cannot have 'numRulesToMatch' greater than the number of configured rules");
	        }

	        if (this.minPasswordLength < MIN_PASSWORD_LENGTH) {
	            throw new IllegalStateException("The configured 'minPasswordLength' cannot be less than " + MIN_PASSWORD_LENGTH);
	        }

	        if (this.maxPasswordLength > MAX_PASSWORD_LENGTH) {
	            throw new IllegalStateException("The configured 'maxPasswordLength' cannot exceed " + MAX_PASSWORD_LENGTH);
	        }

	        if (this.minPasswordLength > this.maxPasswordLength) {
	            throw new IllegalStateException("The configured 'minPasswordLength' cannot exceed the configured 'maxPasswordLength' cannot exceed " + MAX_PASSWORD_LENGTH);
	        }

	        if (this.passwordExpirationDays > MAX_PASSWORD_EXPIRATION_DAYS) {
	            throw new IllegalStateException("The configured 'passwordExpirationDays' cannot exceed " + MAX_PASSWORD_EXPIRATION_DAYS);
	        }

	        if (this.passwordHistoryReuse < 0) {
	            throw new IllegalStateException("The configured 'passwordHistoryReuse' cannot be less than 0");
	        }

	        if (this.passwordHistoryReuse > MAX_PASSWORD_HISTORY_REUSE) {
	            throw new IllegalStateException("The configured 'passwordHistoryReuse' cannot be greater than " + MAX_PASSWORD_HISTORY_REUSE);
	        }

	        if (this.accountLockedAttempts < 0) {
	            throw new IllegalStateException("The configured 'accountLockedAttempts' cannot be less than 0");
	        }

	        if (this.accountLockedAttempts > MAX_ACCOUNT_LOCKED) {
	            throw new IllegalStateException("The configured 'accountLockedAttempts' cannot be greater than " + MAX_ACCOUNT_LOCKED);
	        }
	    }

	    /**
	     * @return True if the password has three identical characters in a row, false otherwise.
	     */
	    private boolean threeInARow() {
	        for (int i = 0; i <= this.newPassword.length() - 3; i++) {
	            int code1 = this.newPassword.charAt(i);
	            int code2 = this.newPassword.charAt(i + 1);
	            int code3 = this.newPassword.charAt(i + 2);
	            if (code1 - code2 == 0 && code1 - code2 == code2 - code3) {
	                return true;
	            }
	        }
	        return false;
	    }

}
