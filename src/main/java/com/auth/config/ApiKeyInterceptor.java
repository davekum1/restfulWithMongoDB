package com.auth.config;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class ApiKeyInterceptor extends HandlerInterceptorAdapter {

	    private String appSecret;

	    public ApiKeyInterceptor(String appSecret) {
	        this.appSecret = appSecret;
	    }

	    @Override
	    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
	        String apiKey = request.getHeader("api-key");
	        if (!this.appSecret.equals(apiKey)) {
	            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	            return false;
	        }

	        String language = request.getHeader("Accept-Language");
	        if (StringUtils.isEmpty(language)) {
	            language = Locale.ENGLISH.getLanguage(); // default to English
	        }
	        LocaleContextHolder.setDefaultLocale(new Locale(language));

	        return true;
	    }

}
