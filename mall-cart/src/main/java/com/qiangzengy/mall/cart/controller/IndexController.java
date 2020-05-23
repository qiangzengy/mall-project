package com.qiangzengy.mall.cart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class IndexController {

    @GetMapping("/{page}.html")
    public String pahe(@PathVariable ("page") String page){
        return page;
    }
}
