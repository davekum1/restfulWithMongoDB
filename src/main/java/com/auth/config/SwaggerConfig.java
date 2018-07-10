package com.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Value("${appinfo.title}")
    private String title;

    @Value("${appinfo.description}")
    private String description;

    @Value("${appinfo.apiVersion}")
    private String version;

    @Value("${appinfo.termsOfServiceUrl}")
    private String termsOfServiceUrl;

    @Value("${appinfo.contact.name}")
    private String contactName;

    @Value("${appinfo.contact.url}")
    private String contactUrl;

    @Value("${appinfo.contact.email}")
    private String contactEmail;

    @Value("${appinfo.license}")
    private String license;

    @Value("${appinfo.licenseUrl}")
    private String licenseUrl;

    @Bean
    public Docket api() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2);
        return docket.select()
            .apis(RequestHandlerSelectors.basePackage("com.auth.controller"))
            .paths(PathSelectors.ant("/api/**"))
            .build().apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
            this.title,
            this.description,
            this.version,
            this.termsOfServiceUrl,
            new Contact(this.contactName, this.contactUrl, this.contactEmail),
            this.license,
            this.licenseUrl);
    }
}