package com.restaurant.waitlist.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SwaggerUiResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations(
                        "classpath:/static/swagger-ui/",
                        "classpath:/META-INF/resources/swagger-ui/"
                )
                .setCachePeriod(0);
    }
}
