package com.qiangzengy.mall.auth.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.qiangzengy.common.constant.AuthConstant;
import com.qiangzengy.common.utils.HttpUtils;
import com.qiangzengy.common.utils.R;
import com.qiangzengy.common.vo.MemberRespVo;
import com.qiangzengy.mall.auth.feign.MemberFeignService;
import com.qiangzengy.mall.auth.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Springsession原理：
 *  @EnableRedisHttpSession导入RedisHttpSessionConfiguration这个配置（@Import(RedisHttpSessionConfiguration.class)）
 * i：给容器添加组件RedisIndexedSessionRepository，redis操作session增删改查的类
 * ii：SessionRepositoryFilter session存储过滤器，每个请求过滤都要经过filter
 * 1。创建的时候，就自动从容器中获取到SessionRepository;
 * 2.原始的request、response都被包装。SessionRepositoryRequsetWrapper SessionRepositoryResponseWrapper
 * 3。以后获取session，requset.getSession()
 * 4.wrappedRequest.getSession()
 *
 * 装饰者模式
 *
 */


@Controller
@RequestMapping("/oauth2/weibo")
public class OAuth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;

    @GetMapping("/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {

        Map<String, String> map = new HashMap<>();
        map. put("client_id","2636917288");
        map. put("client_secret","a263e9284c6c1a74a62adacs11b6e2");
        map. put("grant_type","authorization_code");
        map.put("redirect_uri","http://gulimall.com/oauth2/weibo/success");
        map.put("code",code);
                //根据code换取accessToken
        HttpResponse response = HttpUtils.doPost("api.weibo.com", "/oauth2/access_token", "post",
                null, null, map);
        //获取响应状态码
        if(response.getStatusLine().getStatusCode()==200){
            //获取accessToken
            String s = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(s, SocialUser.class);
            //如何社交用户第一次登陆，自动注册进来，需要为社交用户生成一个会员信息

            R r = memberFeignService.authlogin(socialUser);
            if (r.getCode()==0){
                //登陆成功，跳回首页
                MemberRespVo data = r.getData("data", new TypeReference<MemberRespVo>() {
                });

                /**
                 * 使用Spring session解决session共享问题
                 * 1。默认发的令牌，作用域是当前域(解决子域共享问题)
                 * 2。使用Json序列化来序列化对象存入redis
                 * 在MallSessionConfig实现
                 */
                session.setAttribute(AuthConstant.LOGIN_USER,data);

                return "redirect:http://auth.gulimall.com";
            }else{
                return "redirect:http://auth.gulimall.com/login.html";

            }

        }else {
            return "redirect:http://auth.gulimall.com/login.html";

        }



    }
}
