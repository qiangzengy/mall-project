//package com.qiangzengy.mall.order.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.listener.RedisMessageListenerContainer;
//
///**
// *
// * redis实现延时队列
// * 参考：https://mp.weixin.qq.com/s/enmWh5TUD9_2J82M1ZJLew
// * @author qiangzengy@gmail.com
// * @date 2020/11/10
// */
//
//@Configuration
//public class RedisListenerConfig {
//
//    /**
//     * 定义配置 RedisListenerConfig 实现监听 Redis key 过期时间
//     * @param redisConnectionFactory
//     * @return
//     */
//
//    @Bean
//    RedisMessageListenerContainer container(RedisConnectionFactory redisConnectionFactory){
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        container.setConnectionFactory(redisConnectionFactory);
//        return container;
//    }
//
//}
