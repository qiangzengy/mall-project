package com.qiangzengy.mall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Feign远程调用，丢失请求头的问题
 * 解决：加上Feign远程调用的请求拦截器
 */
@Configuration
public class MallFeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(){

        return requestTemplate -> {
            //RequestContextHolder可以拿到刚进来的请求数据
            ServletRequestAttributes attributes= (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            assert attributes != null;
            HttpServletRequest request = attributes.getRequest();
            //同步请求头数据
            requestTemplate.header("Cookie",request.getHeader("Cookie"));

        };
    }
}
