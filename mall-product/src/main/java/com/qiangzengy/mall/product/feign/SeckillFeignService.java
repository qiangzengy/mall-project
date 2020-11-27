package com.qiangzengy.mall.product.feign;

import com.qiangzengy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/27
 */

@FeignClient("mall-miaosha")
public interface SeckillFeignService {

    @GetMapping("/seckill/current/skus/{skuId}")
    R getSeckillSkus(Long skuId);
}
