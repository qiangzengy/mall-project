package com.qiangzengy.mall.sso.client.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.Objects;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/10/21
 */

@Controller
public class HelloController {

    @Value("${sso.server.url}")
    private String url;

    @ResponseBody
    @RequestMapping("/hello")
    public String hello(HttpSession httpSession){
        Object logUser = httpSession.getAttribute("logUser");
        if (Objects.isNull(logUser)){
            //没登录去登录服务器登录
            return "redirect:"+url+"/login.html"+"?redirect_url=http://sso.server.client1:8081/hello";
        }else {
            return "hello world";
        }
    }


    @ResponseBody
    @GetMapping("/test")
    public String test(){
            return " test";
    }

}
