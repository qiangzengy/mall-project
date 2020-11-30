package com.qiangzengy.mall.product.feign;

import com.qiangzengy.common.utils.R;
import com.qiangzengy.mall.product.feign.fallback.SeckillFeignServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/27
 */

@FeignClient(value = "mall-miaosha",fallback = SeckillFeignServiceFallBack.class)
public interface SeckillFeignService {

    @GetMapping("/seckill/current/skus/{skuId}")
    R getSeckillSkus(Long skuId);
}
