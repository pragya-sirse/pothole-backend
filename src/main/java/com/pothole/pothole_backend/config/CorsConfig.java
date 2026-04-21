package com.pothole.pothole_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.*;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedHeaders(Arrays.asList(
                "Origin","Content-Type","Accept","Authorization",
                "X-Requested-With","Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        config.setAllowedMethods(Arrays.asList(
                "GET","POST","PUT","DELETE","OPTIONS","PATCH"
        ));
        config.setExposedHeaders(Arrays.asList("Authorization","Content-Type"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}