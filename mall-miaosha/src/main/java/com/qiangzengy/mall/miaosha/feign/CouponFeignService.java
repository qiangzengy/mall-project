package com.qiangzengy.mall.miaosha.feign;

import com.qiangzengy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/22
 */

@FeignClient("mill-coupon")
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/getLates3Day")
    R getLates3Day();

}
