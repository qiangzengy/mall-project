package com.qiangzengy.mall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * SpringSession的核心原理（装饰者模式）
 *
 * 1，@EnableRedisHttpSession -> RedisHttpSessionConfiguration配置
 *    i：给容器添加了一个组件RedisIndexedSessionRepository，使用redis操作
 *    ii：SessionRepositoryFilter session存储过滤器，相当于Filter，每个请求过来 ，都要经过Filter
 *        1。创建的时候，就自动从容器中获取到了SessionRepository；
 *        2。原始的request，response都被包装成SessionRepositoryRequestWrapper，SessionRepositoryResponseWrapper
 *        3。以后获取session，request.getSession();
 *        4。但是被包装了，相当于调用wrapperRequest.getSession()，真正是从SessionRepository中获取到的
 *
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableRedisHttpSession //整合redis作为session存储
@EnableFeignClients("com.qiangzengy.mall.auth.feign")
public class MallAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallAuthApplication.class, args);
    }


}
