package com.qiangzengy.mall.ware.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyRabbitConfig {


    /**
     * 使用Json序列化机制，进行消息转换
     * @return
     */

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();

    }

    @Bean
    public Exchange stockEventExchange(){
        return new TopicExchange("stock-event-exchange",true,false);
    }

    @Bean
    public Queue stockReleaseStockQueue(){
        return new Queue("stock.release.stock.queue",true,false,false);
    }

    @Bean
    public Queue stockDelayQueue(){

        /*
         * new Queue(String name, boolean durable, boolean exclusive, boolean autoDelete,
         *                        @Nullable Map<String, Object> arguments);
         */

        /*
         * x-dead-letter-exchange: order-event-exchange
         * x x-dead-letter-routing-key: order.reLease.order
         * x-message-ttl: 60000
         */
        Map<String, Object> arguments=new HashMap<>();
        arguments.put("x-dead-letter-exchange","stock-event-exchange");
        arguments.put("x-dead-letter-routing-key","stock.release");
        arguments.put("x-message-ttl",120000);

        Queue queue=new Queue("stock.delay.queue",true,false,false,arguments);
        return queue;

    }


    @Bean
    public Binding stockReleaseBinding(){

        Binding binding = new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE,
                "stock-event-exchange", "stock.release.#", null);
        return binding;

    }


    @Bean
    public Binding stockLockedBinding(){

        Binding binding = new Binding("stock.delay.queue", Binding.DestinationType.QUEUE,
                "stock-event-exchange", "stock.locked", null);
        return binding;

    }


}
