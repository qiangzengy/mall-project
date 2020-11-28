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




    /**
     * 监听消息
     *
     * queues：声明监听的所有的Queue
     *
     * Queue可以很多人都来监听，只要收到消息，队列删除消息，而且只能有一个收到此消息
     *
     *    1。同一个消息只能有一个客户端收到
     *    2。只有一个消息处理完，才能接收下一个消息
     */
    @RabbitListener(queues = "hello-word-queue")
    void listenerMessage(Message  message,Channel channel){

        System.out.println("监听的消息:"+message);


        //手动ack
        //void basicAck(long deliveryTag, boolean multiple) throws IOException;
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            channel.basicAck(deliveryTag,false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RabbitListener(queues = "order.release.order.queue")
    public void listenerQueue(OrderEntity orderEntity, Channel channel, Message message) throws IOException {

        System.out.println("监听成功。。。。。。。。;订单ID：" + orderEntity.getOrderSn());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    }


    //bean Queue\Binding\Exchange
    @Bean
    public Queue orderDelayQueue() {

        /**
         * new Queue(String name, boolean durable, boolean exclusive, boolean autoDelete,
         *                        @Nullable Map<String, Object> arguments);
         */

        /**
         * x-dead-letter-exchange: order-event-exchange  死信路由
         * x x-dead-letter-routing-key: order.reLease.order  死信路由键
         * x-message-ttl: 1800000 存活时间
         */
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl", 60000);
        return new Queue("order.delay.queue", true, false, false, arguments);

    }

    @Bean
    public Queue orderReleaseOrderQueue() {
        return new Queue("order.release.order.queue", true, false, false);
    }

    @Bean
    public Binding orderCreateOrderBinding() {
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);
    }

    @Bean
    public Binding orderReleaseOrderBinding() {
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);

    }

    @Bean
    public Exchange orderEventExchange() {
        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange("order-event-exchange", true, false);

    }


    @Bean
    public Binding orderReleaseOtherBinding() {
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",
                null);

    }
    //秒杀
    // ======================================================================================


    @Bean
    public Queue orderSeckillOrderQueue() {
        return new Queue("order.seckill.order.queue", true, false, false);
    }

    @Bean
    public Binding orderSeckillOrderBinding() {
        return new Binding("order.seckill.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.order",
                null);
    }


}
