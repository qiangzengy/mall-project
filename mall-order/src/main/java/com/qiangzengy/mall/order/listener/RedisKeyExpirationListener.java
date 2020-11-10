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
 * 修改 redis 相关事件配置。找到 redis 配置文件 redis.conf，查看 notify-keyspace-events 配置项，如果没有，添加 notify-keyspace-events Ex，如果有值，则追加 Ex，相关参数说明如下：
 *
 * K：keyspace 事件，事件以 keyspace@ 为前缀进行发布
 *
 * E：keyevent 事件，事件以 keyevent@ 为前缀进行发布
 *
 * g：一般性的，非特定类型的命令，比如del，expire，rename等
 *
 * $：字符串特定命令
 *
 * l：列表特定命令
 *
 * s：集合特定命令
 *
 * h：哈希特定命令
 *
 * z：有序集合特定命令
 *
 * x：过期事件，当某个键过期并删除时会产生该事件
 *
 * e：驱逐事件，当某个键因 maxmemore 策略而被删除时，产生该事件
 *
 * A：g$lshzxe的别名，因此”AKE”意味着所有事件
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
