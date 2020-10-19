package com.qiangzengy.mall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * SpringMVC viewcontroller,将请求和页面映射过来
 */
@Configuration
public class MallWebController implements WebMvcConfigurer {

    /* 以前是这样，可以优化成下面这样
    @GetMapping("/login.html")
    public String logPage() {
        return "login";
    }
*/
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/reg.html").setViewName("reg");

    }
}
