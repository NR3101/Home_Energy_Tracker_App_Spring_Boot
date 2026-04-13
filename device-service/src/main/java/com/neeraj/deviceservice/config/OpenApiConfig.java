package com.neeraj.deviceservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI deviceServiceOpenAPIDocs() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Device Service API")
                        .description("Documentation for Device Service API for Home Energy Tracker Application")
                        .contact(contact())
                        .license(license())
                        .version("1.0.0"));
    }

    private static License license() {
        return new License()
                .name("Creative Commons Attribution-NonCommercial 4.0 International License")
                .url("https://creativecommons.org/licenses/by-nc/4.0/");
    }

    private static Contact contact() {
        return new Contact()
                .name("Neeraj Rai")
                .email("neeraj@example.com")
                .url("https://www.neerajrai.com");
    }
}
