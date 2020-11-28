package com.qiangzengy.mall.miaosha;

import org.redisson.spring.session.config.EnableRedissonHttpSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients("com.qiangzengy.mall.miaosha.feign")
@EnableRedisHttpSession //整合redis作为session存储
public class MallMiaoShaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallMiaoShaApplication.class, args);
    }

}
