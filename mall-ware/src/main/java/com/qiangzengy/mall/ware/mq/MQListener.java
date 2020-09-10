package com.qiangzengy.mall.ware.mq;

import com.qiangzengy.common.to.mq.OrderEntityTo;
import com.qiangzengy.common.to.mq.StockLockTo;
import com.qiangzengy.mall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RabbitListener(queues = "stock.release.stock.queue")
public class MQListener {

    /**
     * 保持最终一致性
     */

    @Autowired
    private WareSkuService wareSkuService;


    @RabbitHandler
    public void handleStockLockedRelease(StockLockTo to, Message message, Channel channel) throws IOException {

        System.out.println("收到解锁的库存消息;");

        try {
            wareSkuService.unLockStock(to);
            //手动ack
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            //消息拒绝，重新放回队列里面，继续消费解锁
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
    }


    /**
     * 防止订单服务卡顿，导致订单状态消息一直改不了，库存消息优先到期，
     * 查订单处于新建状态，导致库存无法解锁
     * @param orderTo
     * @param message
     * @param channel
     * @throws IOException
     */

    @RabbitHandler
    public void handleOrderCloseRelease(OrderEntityTo orderTo, Message message, Channel channel) throws IOException {

        System.out.println("收到解锁的库存消息;");

        try {
            wareSkuService.unLockStock(orderTo);
            //手动ack
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            //消息拒绝，重新放回队列里面，继续消费解锁
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }
    }
}
