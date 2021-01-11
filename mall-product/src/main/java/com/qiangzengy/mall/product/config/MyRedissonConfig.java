package com.qiangzengy.mall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class MyRedissonConfig {

    /**
     * 所有对redisRedisson的操作，都是使用RedissonClient的对象
     * @return
     */

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private String port;

    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson(){

        Config config = new Config();
        //单节点模式
        config.useSingleServer().setAddress("redis://"+host+":"+port);
        //config.useSingleServer().setAddress("redis://192.168.1.114:6379");
        //集群模式
        /*config.useClusterServers()
                .addNodeAddress("127.0.0.1:7004", "127.0.0.1:7001");*/
        //根据Config创建出RedissonClient实例
        return Redisson.create(config);
    }

}
