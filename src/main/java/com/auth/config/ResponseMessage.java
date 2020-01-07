package com.auth.config;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Convert;

import org.springframework.http.HttpStatus;

import com.google.common.collect.ImmutableMap;


public class ResponseMessage {
	   @Convert(converter = LocalDateTimeConverter.class)
	    protected LocalDateTime timestamp;
	    protected int status;
	    protected Map<String, List<String>> messages = new HashMap<String, List<String>>();
	    protected String path;

	    public ResponseMessage(HttpStatus httpStatus, Map<String, List<String>> messages, String path) {
	        this.timestamp = LocalDateTime.now();
	        this.status = httpStatus.value();
	        if (messages != null && messages.size() > 0) {
	            this.messages = messages;
	        }
	        this.path = path;
	    }

	    public ResponseMessage(HttpStatus httpStatus, String message, String path) {
	        this(httpStatus, ImmutableMap.of("error", Arrays.asList(new String[]{message})), path);
	    }

	    public ResponseMessage(ResponseCode responseCode, String path) {
	        this.timestamp = LocalDateTime.now();
	        this.status = responseCode.getStatus();
	        this.messages.put("error", Arrays.asList(new String[]{responseCode.getMessage()}));
	        this.path = path;
	    }

	    public ResponseMessage(HttpStatus httpStatus, String path) {
	        this.timestamp = LocalDateTime.now();
	        this.status = httpStatus.value();
	        this.path = path;
	    }

}
