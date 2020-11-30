package com.qiangzengy.mall.product.feign.fallback;

import com.qiangzengy.common.utils.R;
import com.qiangzengy.mall.product.feign.SeckillFeignService;
import org.springframework.stereotype.Component;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/30
 */

@Component
public class SeckillFeignServiceFallBack implements SeckillFeignService {
    @Override
    public R getSeckillSkus(Long skuId) {
        return R.error("调用失败");
    }
}
