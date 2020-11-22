package com.qiangzengy.mall.order.listener;

import com.qiangzengy.mall.order.entity.OrderEntity;
import com.qiangzengy.mall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RabbitListener(queues = "order.release.order.queue")
public class OrderListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void orderListente(OrderEntity entity, Message message, Channel channel) throws IOException {

        System.out.println("监听过期的订单");

        try {
            orderService.closeOrder(entity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            //TODO 支付宝手动收单
        }catch (Exception e){

            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);

        }


    }

}
