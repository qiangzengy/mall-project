package com.qiangzengy.mall.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Slf4j
class MallOrderApplicationTests {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    /**
     * 1.创建Exchange、Queue、Binding
     * 2.如何收发消息
     */
    @Test
    void createExchange() {
        DirectExchange exchange=new DirectExchange("hello-word-exchange",true,false);
        amqpAdmin.declareExchange(exchange);
        log.info("创建exchange成功");
    }


    @Test
    void createQueue() {
        Queue queue = new Queue("hello-word-queue",true,false,false);
        amqpAdmin.declareQueue(queue);
        log.info("创建queue成功");

    }

    /**
     * public Binding(String destination, DestinationType destinationType, String exchange, String routingKey,
     *                        @Nullable Map<String, Object> arguments)
     *       destination:目的地,也就是Queue
     *       destinationType：目的地类型
     *       exchange：交换机
     *       routingKey：路由键
     *       arguments：参数
     *     将exchange指定的交换机和destination(目的地)进行绑定，使用routingKey作为指定的路由键
     */
    @Test
    void createBinding() {
        Binding binding = new Binding("hello-word-queue",
                Binding.DestinationType.QUEUE,
                "hello-word-exchange",
                "hello-word",
                null);
        amqpAdmin.declareBinding(binding);

        log.info("binding成功");

    }

    /**
     * 发送消息
     */
    @Test
    void sendMessage(){
        rabbitTemplate.convertAndSend("hello-word-exchange","hello-word","hello world");
    }


}
