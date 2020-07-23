package com.qiangzengy.mall.member.feign;


import com.qiangzengy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("mall-coupon")
public interface CouponFeignService {

    /**
     * 优惠劵列表
     */
    @RequestMapping("/coupon/coupon/list")
    R list(@RequestParam Map<String, Object> params);

}
