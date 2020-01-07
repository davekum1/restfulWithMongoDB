package com.auth.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;

import com.auth.config.LocalDateTimeConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
	    @Id
	    @Column(name = "uuid", insertable = false, updatable = false, nullable = false)
	    @GeneratedValue(generator = "UUID")
	    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
	    private UUID uuid;

	    @Column(name = "login", nullable = false)
	    @NotNull
	    private String login;

	    @Column(name = "email", nullable = false)
	    private String email;

	    @Column(name = "first_name")
	    private String firstName;

	    @Column(name = "last_name")
	    private String lastName;

	    @Column(name = "locked", nullable = false)
	    @NotNull
	    private Boolean locked;

	    @Column(name = "failed_login_attempts", nullable = false)
	    @NotNull
	    private Integer failedLoginAttempts;

	    @Column(name = "password_hash")
	    @JsonIgnore
	    private String passwordHash;

	    @ApiModelProperty(example = "2014-10-09T16:23:00")
	    @Column(name = "password_expiration_date", nullable = false)
	    @Convert(converter = LocalDateTimeConverter.class)
	    @NotNull
	    private LocalDateTime passwordExpirationDate;

	    @Column(name = "password_reset_hash")
	    @JsonIgnore
	    private String passwordResetHash;

	    @ApiModelProperty(example = "2014-10-09T16:23:00")
	    @Column(name = "password_reset_hash_expires")
	    @Convert(converter = LocalDateTimeConverter.class)
	    private LocalDateTime passwordResetHashExpires;

	    public void incrementFailedLoginAttempts() {
	        this.failedLoginAttempts++;
	    }	
}
