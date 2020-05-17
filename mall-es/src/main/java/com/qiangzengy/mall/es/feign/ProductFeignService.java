package com.qiangzengy.mall.es.feign;

import com.qiangzengy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("mall-product")
public interface ProductFeignService {

     @GetMapping("/product/attr/info/{attrId}")
     R attrInfo(@PathVariable("attrId") Long attrId);
}
