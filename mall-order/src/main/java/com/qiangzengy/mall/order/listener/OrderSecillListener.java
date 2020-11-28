package com.qiangzengy.mall.order.listener;

import com.qiangzengy.common.to.mq.SeckillOrderTo;
import com.qiangzengy.mall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/28
 */

@Component
@RabbitListener(queues = "order.seckill.order.queue")
@Slf4j
public class OrderSecillListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void seckillListente(SeckillOrderTo seckillOrderTo, Message message, Channel channel) throws IOException {
        log.info("监听秒杀订单，message：{}",message);
        try {
            //创建秒杀订单
            orderService.createSeckillOrder(seckillOrderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);

            //TODO 支付宝手动收单
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }


    }
}
