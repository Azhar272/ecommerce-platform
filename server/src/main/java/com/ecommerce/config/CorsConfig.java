package com.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 * 允许前端（Vue 项目运行在 5173 端口）访问后端（8080 端口）
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);              // 允许携带 Cookie
        config.addAllowedOriginPattern("*");           // 允许所有来源
        config.addAllowedHeader("*");                  // 允许所有请求头
        config.addAllowedMethod("*");                  // 允许所有请求方法（GET/POST/PUT/DELETE等）

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 对所有路径生效
        return new CorsFilter(source);
    }
}
