package com.auth.config;

public class RequestParamConstant {
	    public final static String LOGIN = "login";
	    public final static String LOGIN_DESC = "User login information";

	    public final static String ACCEPT_LANGUAGE = "Accept-Language";
	    public final static String ACCEPT_LANGUAGE_DESC = "Accept-Language";

	    public final static String PASSWORD = "password";
	    public final static String PASSWORD_DESC = "User password";

	    public final static String CURRENT_PASSWORD = "currentPassword";
	    public final static String CURRENT_PASSWORD_DESC = "Current password";

	    public final static String NEW_PASSWORD = "newPassword";
	    public final static String NEW_PASSWORD_DESC = "New password";

	    public final static String CONFIRMED_PASSWORD = "confirmedPassword";
	    public final static String CONFIRMED_PASSWORD_DESC = "Confirm new password";

	    public final static String PASSWORD_RESET_HASH = "passwordResetHash";
	    public final static String PASSWORD_RESET_HASH_DESC = "Password reset hash";

	    public final static String EMAIL = "email";
	    public final static String EMAIL_DESC = "User email";

	    public final static String USER_REQUEST_DATA = "userRequestData";
	    public final static String USER_REQUEST_DATA_DESC = "User request data";

	    public final static String PAGE_NUMBER      = "page";
	    public final static String PAGE_NUMBER_DESC = "Page number contain search result. Start with zero (0). This page number is "
	                                                + "dynamically calculated by the page size. Default value is zero (0)";

	    public final static String SIZE = "size";
	    public final static String SIZE_DESC = "Total maximum number allow in one page. Default value is 100.";

}