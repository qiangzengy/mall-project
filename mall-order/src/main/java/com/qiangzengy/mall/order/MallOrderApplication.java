package com.qiangzengy.mall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 使用 RabbitMQ
 * 1、引入amgp场景; RabbitAutoConfiguration就会自动生效
 *
 * 2、给容器中自动配置了
 *     RabbitTemplate、 AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate
 *     所有的属性都是 spring.rabbitmq
 *     ConfigurationProperties(prefix ="spring, rabbitmq")
 *     pubLic cLass RabbitProperties
 *
 * 3、给配置文件中配置 spring, rabbitmq信息
 *
 * 4、@Enablerabb1: @EnabLeXxxXX;开启功能
 *
 * 5、监听消息:使用 @Rabbitlistener；必须有@EnableRabbit
 */

@EnableDiscoveryClient
@SpringBootApplication
@EnableRabbit
@MapperScan("com.qiangzengy.mall.order.dao")
@EnableFeignClients("com.qiangzengy.mall.order.feign")
public class MallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallOrderApplication.class, args);
    }

}
