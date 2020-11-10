package com.qiangzengy.mall.order.listener;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 *
 * redis实现延时队列
 * 参考：https://mp.weixin.qq.com/s/enmWh5TUD9_2J82M1ZJLew
 *
 * @author qiangzengy@gmail.com
 * @date 2020/11/10
 *
 * 监听所有db的过期事件__keyevent@*__:expired"
 */

@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 针对 redis 数据失效事件，进行数据处理
     * @param message
     * @param pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 获取到失效的 key，进行取消订单业务处理
        String expiredKey = message.toString();
        System.out.println("============expiredKey："+expiredKey);
    }
}
