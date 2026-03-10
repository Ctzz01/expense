package com.expense.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Prefer the UI from the local expense-tracker folder, then fall back to classpath
        registry.addResourceHandler("/**")
                .addResourceLocations(
                        "file:expense-tracker/",
                        "file:expense-tracker/assets/",
                        "classpath:/static/")
                .setCacheControl(org.springframework.http.CacheControl.noCache());
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
