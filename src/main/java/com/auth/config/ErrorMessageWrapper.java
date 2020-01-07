package com.auth.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErrorMessageWrapper {
	 private Map<String, List<String>> errorMessages = new HashMap<String, List<String>>();
	    private String fieldKey;

	    /**
	     * Field key definition in the message properties
	     * For example:
	     * In message.properties, there is definition as below
	     * password.mustmatch=Passwords do not match.
	     *
	     * "password" is the fieldKey that should be passed in the constructor to make this holder that contain all error messages
	     * for password related properties
	     *
	     * @param fieldKey key in message properties
	     */
	    public ErrorMessageWrapper(String fieldKey) {
	        this.fieldKey = fieldKey;
	    }

	    public Map<String, List<String>> getErrorMessages() {
	        return errorMessages;
	    }

	    public List<String> getErrorMessagesAsList() {
	        List<String> list = new ArrayList<String>();
	        for (List<String> values: errorMessages.values()) {
	            for (String value: values) {
	                list.add(value);
	            }
	        }
	        return list;
	    }

	    public void setErrorMessages(Map<String, List<String>> errorMessages) {
	        this.errorMessages = errorMessages;
	    }

	    public void addErrorMessage(String columnKey, String fieldString, Object... fields) {
	        String message = MessageResource.getInstance().resolveMessage(this.fieldKey + "." + fieldString, fields);
	        List<String> currentMessages = this.errorMessages.get(columnKey) != null ? this.errorMessages.get(columnKey) : new ArrayList<String>();
	        currentMessages.add(message);
	        this.errorMessages.put(columnKey, currentMessages);
	    }


}
