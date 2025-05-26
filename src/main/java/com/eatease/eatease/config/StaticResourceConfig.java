package com.eatease.eatease.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded images from uploads/items/ directory
        registry.addResourceHandler("/uploads/items/**")
                .addResourceLocations("file:uploads/items/")
                .setCachePeriod(3600); // Cache for 1 hour
    }
}
