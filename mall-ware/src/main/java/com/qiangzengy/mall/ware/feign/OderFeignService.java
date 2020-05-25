package com.qiangzengy.mall.ware.feign;

import com.qiangzengy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("mall-order")
public interface OderFeignService {


    @GetMapping("/order/order/status/{orderSn}")
    R getStatusByOrderSn(@PathVariable("orderSn") String orderSn);
}
