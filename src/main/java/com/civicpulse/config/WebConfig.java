package com.civicpulse.config;

import com.civicpulse.enums.Urgency;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
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
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, Urgency.class, source -> {
            if (source == null || source.isBlank()) return Urgency.MEDIUM;
            return switch (source.trim()) {
                case "1" -> Urgency.LOW;
                case "2" -> Urgency.MEDIUM;
                case "3" -> Urgency.HIGH;
                case "4" -> Urgency.CRITICAL;
                default -> {
                    try {
                        yield Urgency.valueOf(source.trim().toUpperCase());
                    } catch (Exception e) {
                        yield Urgency.MEDIUM;
                    }
                }
            };
        });
    }
}
