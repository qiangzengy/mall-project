package com.qiangzengy.mall.product.web;

import com.qiangzengy.mall.product.entity.vo.SkuItemVo;
import com.qiangzengy.mall.product.service.SkuInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {

    @Autowired
    private SkuInfoService skuInfoService;
    
    @GetMapping("/{skuId}.html")
    public String itemPage(@PathVariable("skuId") Long skuId){

        SkuItemVo itemVo=skuInfoService.item(skuId);

        return "item";
    }




}
