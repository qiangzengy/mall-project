package com.qiangzengy.mall.miaosha.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/22
 */

@Configuration
@EnableScheduling
@EnableAsync
@ConfigurationProperties(prefix = "task")
@Data
public class TaskConfig {

    private String runIp;
}
