package com.qiangzengy.mall.order.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/10/28
 */

@Configuration
public class MyRabbitMQ {

    /**
     * 消息类型转换器
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }



}
