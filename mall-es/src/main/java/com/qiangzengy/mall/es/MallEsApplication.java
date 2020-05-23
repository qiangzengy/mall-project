package com.qiangzengy.mall.es;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 对es的相关操作参考：
 * https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.4/java-rest-high-getting-started.html
 */
@EnableRedisHttpSession
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients("com.qiangzengy.mall.es.feign")
public class MallEsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallEsApplication.class, args);
    }

}
