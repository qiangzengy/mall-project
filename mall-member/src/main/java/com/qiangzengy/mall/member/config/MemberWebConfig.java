package com.qiangzengy.mall.member.config;

import com.qiangzengy.mall.member.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/21
 */
public class MemberWebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginUserInterceptor loginUserInterceptor;

    /**
     * 添加拦截器，让LoginUsetInterceptors起作用
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {


        //添加拦截器
        registry.addInterceptor(loginUserInterceptor)
                //所有请求都要用拦截器进行拦截
                .addPathPatterns("/**");

    }
}
