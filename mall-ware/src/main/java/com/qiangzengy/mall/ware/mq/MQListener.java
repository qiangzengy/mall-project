package com.qiangzengy.mall.ware.mq;

import com.alibaba.fastjson.TypeReference;
import com.qiangzengy.common.to.mq.StockDetailTo;
import com.qiangzengy.common.to.mq.StockLockTo;
import com.qiangzengy.common.utils.R;
import com.qiangzengy.mall.ware.dao.WareSkuDao;
import com.qiangzengy.mall.ware.entity.WareOrderTaskDetailEntity;
import com.qiangzengy.mall.ware.entity.WareOrderTaskEntity;
import com.qiangzengy.mall.ware.feign.OderFeignService;
import com.qiangzengy.mall.ware.service.WareOrderTaskService;
import com.qiangzengy.mall.ware.service.WareSkuService;
import com.qiangzengy.mall.ware.service.impl.WareOrderTaskDetailServiceImpl;
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
}
