package com.auth.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.data.domain.Sort;

public class WebConfig  extends WebMvcConfigurerAdapter  {
	 @Value("${spring.application.appSecret}")
	    private String appSecret;

	    @Bean
	    public RestTemplate restTemplate() {
	        return new RestTemplate();
	    }

	    @Bean(name = "messageSource")
	    public ReloadableResourceBundleMessageSource messageSource() {
	        ReloadableResourceBundleMessageSource messageBundle = new ReloadableResourceBundleMessageSource();
	        messageBundle.setBasename("classpath:messages");
	        messageBundle.setDefaultEncoding("UTF-8");

	        return messageBundle;
	    }

	    @Override
	    public void addInterceptors(InterceptorRegistry registry) {
	        registry
	            .addInterceptor(new ApiKeyInterceptor(this.appSecret))
	            .addPathPatterns("/api/**");
	    }

	    /**
	     * This adds default parameters for page, size, and sort if they are not present in the URL:
	     * http://localhost:8080/api/user?page=0&size=100&sort=uuid%2CASC
	     *
	     * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter#addArgumentResolvers(java.util.List)
	     */
	    @Override
	    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
	        PageableHandlerMethodArgumentResolver requestParamResolver = new PageableHandlerMethodArgumentResolver();
	        requestParamResolver.setFallbackPageable(new PageRequest(0, 100, Sort.Direction.ASC, "uuid"));
	        // If greater than this, this just overwrites what the user set in the URL
	        requestParamResolver.setMaxPageSize(1000);
	        argumentResolvers.add(requestParamResolver);
	        super.addArgumentResolvers(argumentResolvers);
	    }

}
