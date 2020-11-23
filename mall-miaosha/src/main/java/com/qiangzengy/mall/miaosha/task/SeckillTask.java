package com.qiangzengy.mall.miaosha.task;

import com.qiangzengy.mall.miaosha.config.TaskConfig;
import com.qiangzengy.mall.miaosha.service.SeckillService;
import com.qiangzengy.mall.miaosha.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/22
 */
@Component
@Slf4j
public class SeckillTask {

    @Autowired
    private TaskConfig taskConfig;
    @Autowired
    private SeckillService seckillService;

    @Scheduled(cron = "${task.seckill}")
    public void seckillUpTask(){


        String runIp = taskConfig.getRunIp();
        try {
            String realIp = IpUtil.getRealIp();
            log.info("当前ip地址：{}", realIp);
            if (!runIp.equals(realIp)) {
                return;
            }
            seckillService.seckillGoodsUp();
            log.info("矿机抽签任务 start");
            log.info("矿机抽签任务 end");
        } catch (Exception e) {
            log.warn("矿机抽签任务异常", e);
        }

    }


}
