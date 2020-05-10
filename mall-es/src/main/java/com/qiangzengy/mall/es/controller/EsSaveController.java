package com.qiangzengy.mall.es.controller;

import com.qiangzengy.common.enums.ExceptionCode;
import com.qiangzengy.common.to.es.SkuEsModel;
import com.qiangzengy.common.utils.R;
import com.qiangzengy.mall.es.service.EsSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/search/save")
public class EsSaveController {

    @Autowired
    private EsSaveService esSaveService;

    @RequestMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels){
        boolean b;
        try {
            b = esSaveService.productStatusUp(skuEsModels);
        }catch (Exception e){
            log.error("商品上架错误",e);
            return R.error(ExceptionCode.PRODUCT_UP_EXCEPTION.getCode(),ExceptionCode.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if(b){
            return R.ok();
        }else {
            return R.error(ExceptionCode.PRODUCT_UP_EXCEPTION.getCode(),ExceptionCode.PRODUCT_UP_EXCEPTION.getMsg());

        }
    }



}
