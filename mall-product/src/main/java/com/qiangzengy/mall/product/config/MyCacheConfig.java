package com.qiangzengy.mall.product.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class MyCacheConfig {

    /**
     * 配置文件的东西没有用上
     * 解决：需要加 @EnableConfigurationProperties(CacheProperties.class)
     * @return
     */

    //@Autowired
    //private CacheProperties cacheProperties;

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties){
        RedisCacheConfiguration configuration=RedisCacheConfiguration.defaultCacheConfig();
        //key的序列化
        configuration=configuration.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));
        configuration=configuration.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericFastJsonRedisSerializer()));

        CacheProperties.Redis redisProperties = cacheProperties.getRedis();
        //需要将配置文件的东西也拿来
        if (redisProperties.getTimeToLive()!=null){
            configuration=configuration.entryTtl(redisProperties.getTimeToLive());
        }
        if (redisProperties.getKeyPrefix()!=null){
            configuration=configuration.prefixKeysWith(redisProperties.getKeyPrefix());
        }
        if (redisProperties.getTimeToLive()!=null){
            configuration=configuration.disableCachingNullValues();
        }
        if (redisProperties.getTimeToLive()!=null){
            configuration=configuration.disableKeyPrefix();
        }
        return configuration;
    }

}
