package com.qiangzengy.mall.cart.config;

import com.qiangzengy.mall.cart.interceptor.CartInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class MallWebConfig implements WebMvcConfigurer {

    /**
     * 1.添加CartInterceptor拦截器
     * 2.拦截所有请求.addPathPatterns("/**")
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CartInterceptor())
        .addPathPatterns("/**");

    }
}
