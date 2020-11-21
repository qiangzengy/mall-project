package com.qiangzengy.mall.member.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/21
 */
@Controller
public class MemberWebController {

    @GetMapping("/memberOrder.html")
    public String memberPage(){
        return "orderList";
    }


}
