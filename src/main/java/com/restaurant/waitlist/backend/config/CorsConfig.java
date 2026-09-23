package com.restaurant.waitlist.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                        "http://localhost:4200",
                        "https://dev.dinerly.ca"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Spring Boot's automatic index.html resolution only applies at the static
        // resource root ("/"), not nested folders — forward explicitly so both
        // "/docs" and "/docs/" serve the Scalar API reference page.
        registry.addViewController("/docs").setViewName("forward:/docs/index.html");
        registry.addViewController("/docs/").setViewName("forward:/docs/index.html");
    }
}

