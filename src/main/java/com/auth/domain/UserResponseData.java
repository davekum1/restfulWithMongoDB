package com.auth.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Convert;

import com.auth.config.LocalDateTimeConverter;

import io.swagger.annotations.ApiModelProperty;

public class UserResponseData {
	    private UUID uuid;
	    private Long profileId;
	    private String login;
	    private String email;
	    private String firstName;
	    private String lastName;
	    private Boolean locked;
	    private Integer failedLoginAttempts;

	    @ApiModelProperty(example = "2014-10-09T16:23:00")
	    @Convert(converter = LocalDateTimeConverter.class)
	    private LocalDateTime passwordExpirationDate;

	    @ApiModelProperty(example = "2014-10-09T16:23:00")
	    @Convert(converter = LocalDateTimeConverter.class)
	    private LocalDateTime passwordResetHashExpires;

	    private String dateFormat;
	    private String timeFormat;
	    private String timezone;
	    private String locale;

	    @ApiModelProperty(example = "2014-10-09T16:23:00.000")
	    @Convert(converter = LocalDateTimeConverter.class)
	    private LocalDateTime dateCreated;

	    @ApiModelProperty(example = "2014-10-09T16:23:00.000")
	    @Convert(converter = LocalDateTimeConverter.class)
	    private LocalDateTime dateModified;

	    public void incrementFailedLoginAttempts() {
	        this.failedLoginAttempts++;
	    }
}
