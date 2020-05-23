package com.qiangzengy.mall.auth.feign;

import com.qiangzengy.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("mall-third-party")
public interface ThirdPartyFeignService {

    @GetMapping("/sms/send")
     R sendMsm(@RequestParam("phone") String phone,@RequestParam("code") String code);
}
