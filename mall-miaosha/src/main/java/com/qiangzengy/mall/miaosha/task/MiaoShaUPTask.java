package com.qiangzengy.mall.miaosha.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/22
 */

@Component
@EnableScheduling//开启定时任务
@EnableAsync //开启定时任务异步任务
@Slf4j
public class MiaoShaUPTask {

    /**
     * Scheduled 不支持年
     */
    //@Scheduled(cron = "* * * * * *")
    //@Async//异步执行
    public void upTask(){
        log.info("定时任务启动。。。。。");
    }

}
