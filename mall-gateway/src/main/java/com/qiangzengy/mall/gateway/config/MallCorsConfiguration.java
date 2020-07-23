package com.qiangzengy.mall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 配置统一跨域
 */
@Configuration
public class MallCorsConfiguration {

    //CorsWebFilter Spring 提供的解决跨域请求

    @Bean
    public CorsWebFilter corsWebFilter(){

        //CorsConfigurationSource
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //允许哪些头进行跨域
        corsConfiguration.addAllowedHeader("*");
        //允许哪些请求方式
        corsConfiguration.addAllowedMethod("*");
        //允许哪些请求来源
        corsConfiguration.addAllowedOrigin("*");
        //是否允许携带cookie
        corsConfiguration.setAllowCredentials(true);
        source.registerCorsConfiguration("/**",corsConfiguration);
        return new CorsWebFilter(source);
    }
}
