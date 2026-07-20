package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置
 * 注册登录拦截器，配置哪些路径需要登录验证
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")                    // 拦截所有 /api/ 开头的请求
                .excludePathPatterns(                          // 排除不需要登录的接口
                        "/api/users/register",                  // 注册
                        "/api/users/login",                     // 登录
                        "/api/products",                        // 商品列表
                        "/api/products/*",                      // 商品详情
                        "/api/categories",                      // 分类列表
                        "/api/products/*/reviews"              // 评价列表
                );
    }
}
