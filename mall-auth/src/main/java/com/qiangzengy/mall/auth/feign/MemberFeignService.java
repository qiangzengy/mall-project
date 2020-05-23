package com.qiangzengy.mall.auth.feign;

import com.qiangzengy.common.utils.R;
import com.qiangzengy.mall.auth.vo.SocialUser;
import com.qiangzengy.mall.auth.vo.UserLogVo;
import com.qiangzengy.mall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("mall-mamber")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo memBerRegistVo);

    @PostMapping("/member/member/login")
     R login(@RequestBody UserLogVo logVo);

    @PostMapping("/member/member/oauth2/login")
     R authlogin(@RequestBody SocialUser member)throws Exception;
}
