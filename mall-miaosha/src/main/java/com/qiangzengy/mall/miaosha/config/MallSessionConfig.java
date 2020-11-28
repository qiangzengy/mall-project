package com.qiangzengy.mall.miaosha.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class MallSessionConfig {

    @Bean
    public CookieSerializer cookieSerializer(){
        DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();
        //设置作用域，放大到父域名
        defaultCookieSerializer.setDomainName("gulimall.com");
        //改变cookie名字
        defaultCookieSerializer.setCookieName("GULISESSION");

        return defaultCookieSerializer;
    }


    //redis序列化
    @Bean
    public RedisSerializer<Object> redisSerializer(){
        return new GenericJackson2JsonRedisSerializer();
    }
}
