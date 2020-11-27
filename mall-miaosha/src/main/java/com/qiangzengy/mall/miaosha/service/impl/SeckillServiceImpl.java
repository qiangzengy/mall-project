package com.qiangzengy.mall.miaosha.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.qiangzengy.common.to.SessionRedisTo;
import com.qiangzengy.common.utils.R;
import com.qiangzengy.common.vo.SeckillSessionVo;
import com.qiangzengy.common.vo.SeckillSkuInfoVo;
import com.qiangzengy.common.vo.SeckillSkuRelationVo;
import com.qiangzengy.mall.miaosha.feign.CouponFeignService;
import com.qiangzengy.mall.miaosha.feign.ProductFeignService;
import com.qiangzengy.mall.miaosha.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/22
 */
@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private ProductFeignService productFeignService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    //活动的key
    private final String SESSION_PREFIX = "seckill:sessions:";

    //活动商品的key
    private final String SESSION_ITEM = "seckill:skus:";

    //redis信号量
    private final String SKU_STOCK_SEMAPHORE = "seckill:tock:";

    @Override
    public void seckillGoodsUp() {
        // 扫描最近3天的秒杀活动
        R lates3Day = couponFeignService.getLates3Day();
        if (lates3Day.getCode() == 0) {
            List<SeckillSessionVo> list = lates3Day.getData("data", new TypeReference<List<SeckillSessionVo>>() {
            });
            // 放入redis
            // 缓存活动信息
            saveSessInfos(list);
            // 缓存活动的商品信息
            saveSessionSkuInfos(list);
        }
    }

    private void saveSessInfos(List<SeckillSessionVo> list) {
        list.stream().forEach(session -> {
            long startTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            // seckill:sessions:21488948339_21489998339
            String key = startTime + "_" + endTime;
            Boolean aBoolean = stringRedisTemplate.hasKey(key);
            if (!aBoolean) {
                // 获取所有的商品id
                List<String> collect = session.getSeckillSkuRelationEntities().stream().map(data -> data.getPromotionSessionId() + "_" + data.getSkuId()).collect(Collectors.toList());
                // 从左边放
                stringRedisTemplate.opsForList().leftPushAll(SESSION_PREFIX + key);
                log.info("缓存活动信息,key:{},data:{}",SESSION_PREFIX + key,collect);
            }

        });

    }

    private void saveSessionSkuInfos(List<SeckillSessionVo> list) {
        list.stream().forEach(session -> {
            // hash
            BoundHashOperations<String, String, String> operations = stringRedisTemplate.boundHashOps(SESSION_ITEM);
            // 商品信息
            List<SeckillSkuRelationVo> seckillSkuRelationEntities = session.getSeckillSkuRelationEntities();
            for (SeckillSkuRelationVo entity : seckillSkuRelationEntities) {
                String randomCode = UUID.randomUUID().toString().replace("_", "");
                Boolean aBoolean = stringRedisTemplate.hasKey(entity.getPromotionSessionId() + "_" + entity.getSkuId());
                if (!aBoolean) {
                    SessionRedisTo sessionRedisTo = new SessionRedisTo();
                    // 商品详细信息
                    R info = productFeignService.info(entity.getSkuId());
                    if (info.getCode() == 0) {
                        SeckillSkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SeckillSkuInfoVo>() {
                        });
                        sessionRedisTo.setSeckillSkuInfoVo(skuInfo);
                    }
                    // 秒杀数据
                    BeanUtils.copyProperties(entity, sessionRedisTo);
                    sessionRedisTo.setStartTime(session.getStartTime().getTime());
                    sessionRedisTo.setEndTime(session.getEndTime().getTime());
                    // 随机码,防止恶意攻击
                    sessionRedisTo.setRandomCode(randomCode);
                    operations.put(entity.getPromotionSessionId() + "_" + entity.getSkuId(), JSON.toJSONString(sessionRedisTo));
                    String key = operations.get(entity.getPromotionSessionId() + "_" + entity.getSkuId());
                    log.info("缓存活动的商品信息,key:{},data:{}",entity.getPromotionSessionId() + "_" + entity.getSkuId(),key);
                    // 得到redis信号量
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + randomCode);
                    // 设置信号量,商品秒杀数量,限流
                    semaphore.trySetPermits(entity.getSeckillCount());
                }

            }
        });
    }

    @Override
    public List<SessionRedisTo> getCurrentSeckillSkus() {
        //确定当前时间属于那个秒杀场次
        long time = new Date().getTime();
        Set<String> keys = stringRedisTemplate.keys(SESSION_PREFIX + "*");
        for (String key : keys) {
            log.info("key:{}",key);
            String replace = key.replace(SESSION_PREFIX, "");
            String[] s = replace.split("_");
            long startTime = Long.parseLong(s[0]);
            long endTime = Long.parseLong(s[1]);
            //符合秒杀场次
            if (time >= startTime && time <= endTime) {
                //获取商品数据
                List<String> range = stringRedisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> operations = stringRedisTemplate.boundHashOps(SESSION_ITEM);
                List<String> list = operations.multiGet(range);
                if (Objects.nonNull(list)){
                  return   list.stream().map(item ->
                       JSON.parseObject(item, SessionRedisTo.class)
                    ).collect(Collectors.toList());
                }
                break;
            }
        }
        return null;
    }


    @Override
    public SessionRedisTo getSeckillSkus(Long skuId) {
        BoundHashOperations<String, String, String> operations = stringRedisTemplate.boundHashOps(SESSION_ITEM);
        Set<String> keys = operations.keys();
        String reg="\\d_"+skuId;
        for (String key : keys) {
            if (Pattern.matches(reg, key)){
                String s = operations.get(key);
                return JSON.parseObject(s, SessionRedisTo.class);
            }
        }
        return null;
    }
}
