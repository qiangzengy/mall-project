package com.qiangzengy.mall.mallssoservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/10/21
 */

@Controller
public class LoginController {

    @PostMapping("/login")
    public String login(@RequestParam("name") String name,
                        @RequestParam("password") String password,
                        @RequestParam("url") String url,
                        HttpServletResponse response){
        if (!StringUtils.isEmpty(name) && !StringUtils.isEmpty(password)){

            String replace = UUID.randomUUID().toString().replace("_", "");
            Cookie sso_token = new Cookie("sso_token", replace);
            response.addCookie(sso_token);
            //登录成功，跳回之前页面
            return "redirect:"+url+"?token="+replace;
        }
        return "login";
    }


    @GetMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String url, Model model,@CookieValue(value = "sso_token",required = false) String sso_token){
        if (StringUtils.isEmpty(sso_token)){
            //说明已经登录了
            return "redirect:"+url+"?token="+sso_token;
        }
        model.addAttribute("url",url);
        return "login";
    }

}
