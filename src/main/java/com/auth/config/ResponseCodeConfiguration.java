package com.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResponseCodeConfiguration {
    @Value("${responseCode.passwordExpired}")
    @lombok.Getter
    private int passwordExpired;

    @Value("${responseCode.accountLocked}")
    @lombok.Getter
    private int accountLocked;

    @Value("${responseCode.accountJustBecameLocked}")
    @lombok.Getter
    private int accountJustBecameLocked;
}