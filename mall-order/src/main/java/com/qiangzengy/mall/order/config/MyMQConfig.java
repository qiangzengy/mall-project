package com.qiangzengy.mall.order.config;

import com.qiangzengy.mall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyMQConfig {


    @RabbitListener(queues = "order.release.order.queue")
    public void listenerQueue(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        System.out.println("监听成功。。。。。。。。;订单ID："+orderEntity.getOrderSn());

        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);

    }



    //bean Queue\Binding\Exchange
    @Bean
    public Queue orderDelayQueue(){

        /**
         * new Queue(String name, boolean durable, boolean exclusive, boolean autoDelete,
         *                        @Nullable Map<String, Object> arguments);
         */

        /**
         * x-dead-letter-exchange: order-event-exchange
         * x x-dead-letter-routing-key: order.reLease.order
         * x-message-ttl: 60000
         */
        Map<String, Object> arguments=new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl",60000);

        Queue queue=new Queue("order.delay.queue",true,false,false,arguments);
        return queue;

    }

    @Bean
    public Queue orderReleaseOrderQueue(){

        Queue queue=new Queue("order.release.order.queue",true,false,false);
        return queue;
    }

    @Bean
    public Binding orderCreateOrderBinding(){

        Binding binding = new Binding("order.delay.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.create.order", null);
        return binding;
    }

    @Bean
    public Binding orderReleaseOrderBinding(){

        Binding binding = new Binding("order.release.order.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.release.order", null);
        return binding;

    }

    @Bean
    public Exchange orderEventExchange(){

       // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        TopicExchange topicExchange = new TopicExchange("order-event-exchange", true, false);
        return topicExchange;

    }


    @Bean
    public Binding orderReleaseOtherBinding(){

        Binding binding = new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE,
                "order-event-exchange", "order.release.other.#", null);
        return binding;

    }
}
