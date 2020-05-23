package com.qiangzengy.mall.cart.feign;

import com.qiangzengy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("mall-product")
public interface ProductFeignService {

    @RequestMapping("/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

    @GetMapping("/product/skusaleattrvalue/strList/{skuId}")
     List<String> getSkuSaleAttrValues(@PathVariable ("skuId") Long skuId);
}
