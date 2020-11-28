package com.qiangzengy.mall.miaosha.task;

import com.qiangzengy.mall.miaosha.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/22
 */
@Component
@Slf4j
@EnableScheduling
@EnableAsync
public class SeckillTask {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 上架最近3天的秒杀信息
     */
    @Scheduled(cron = "0 2 17 * * ?")
    @Async
    public void seckillUpTask(){
        //幂等性处理，分布式锁

        RLock lock = redissonClient.getLock("seckill:up:task");
        lock.lock(10, TimeUnit.SECONDS);
        try {
            log.info("秒杀任务上架开始。。。。。");
            seckillService.seckillGoodsUp();
            log.info("秒杀任务上架完成。。。。。");
        }finally {
            lock.unlock();
        }

    }


}
