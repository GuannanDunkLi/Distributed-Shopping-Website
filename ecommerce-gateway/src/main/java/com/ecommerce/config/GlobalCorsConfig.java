package com.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class GlobalCorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        // 1.Add CORS configuration information
        CorsConfiguration config = new CorsConfiguration();
        // 1) Allowed domains
        config.addAllowedOrigin("http://manage.leyou.com");
        config.addAllowedOrigin("http://www.leyou.com");
        // 2) Whether to send cookie information
        config.setAllowCredentials(true);
        // 3) Allowed request method
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("HEAD");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        // 4）Allowed header information
        config.addAllowedHeader("*");
        // 5) Max time
        config.setMaxAge(3600L);
        // 2.Add a mapping path and intercept all requests
        UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
        configSource.registerCorsConfiguration("/**", config);
        // 3.Return the new CorsFilter
        return new CorsFilter(configSource);
    }
}