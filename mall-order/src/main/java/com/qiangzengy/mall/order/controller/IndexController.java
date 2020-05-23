package com.qiangzengy.mall.order.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class IndexController {

    @GetMapping("/{index}.html")
    public String index(@PathVariable("index") String index ){
        return index;

    }

}
