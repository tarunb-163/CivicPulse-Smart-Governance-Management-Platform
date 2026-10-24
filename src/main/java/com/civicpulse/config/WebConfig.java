package com.civicpulse.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0); // for active dev/internship testing
    }

    @Override
    public void addFormatters(org.springframework.format.FormatterRegistry registry) {
        registry.addConverter(String.class, com.civicpulse.enums.Urgency.class, source -> {
            if (source == null || source.isBlank()) return com.civicpulse.enums.Urgency.MEDIUM;
            return switch (source.trim()) {
                case "1" -> com.civicpulse.enums.Urgency.LOW;
                case "2" -> com.civicpulse.enums.Urgency.MEDIUM;
                case "3" -> com.civicpulse.enums.Urgency.HIGH;
                case "4" -> com.civicpulse.enums.Urgency.CRITICAL;
                default -> {
                    try {
                        yield com.civicpulse.enums.Urgency.valueOf(source.trim().toUpperCase());
                    } catch (Exception e) {
                        yield com.civicpulse.enums.Urgency.MEDIUM;
                    }
                }
            };
        });
    }
}
