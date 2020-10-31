package com.qiangzengy.mall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/10/28
 */

@Configuration
public class MyRabbitMQConfig {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 消息类型转换器
     */
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    /**
     *定制rabbitTemplate
     * 保证服务器收到消息：
     * 第一步：spring.rabbitmq.publisher-confirms=true
     * 第二步：设置确认回调
     * 消息抵达Queue：
     *  第一步：spring.rabbitmq.publisher-returns=true
     *         spring.rabbitmq.template.mandatory=true
     *  第二步：设置消息抵达队列回调
     * 消费端确认：（保证每个消息被正确消费，此时才可以broken删除此消息）
     *     默认是自动提交ack，只要消息接收到，就会被移除（存在的问题，可能消息还没被成功处理,导致丢消息丢失)
     *     解决方案：手动提交ack
     *     spring.rabbitmq.listener.direct.acknowledge-mode=manual
     *
     */
    @PostConstruct //MyRabbitMQ对象创建完成以后，执行这个方法
    public void initRabbitTemplate(){
        //设置确认回调
       /* rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {

            }
        });*/

        /*
         *
         * 只要消息抵达Broker，ack就是true
         * correlationData ：当前消息的唯一关联数据（消息的唯一id）
         * ack：消息是否成功收到
         * cause：失败的原因
         */
        //用lambda表达式实现
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            System.out.println("确认回调，cause:"+cause);
        });


        //设置消息抵达队列回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {

            /**
             *
             * @param message 投递失败的消息的详细信息
             * @param replyCode  回复的状态码
             * @param replyText  回复的文本内容
             * @param exchange   消息发给哪个交换机
             * @param routingKey  消息用的哪个路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("失败的消息："+message+"，exchange："+exchange);

            }
        });
    }



}
