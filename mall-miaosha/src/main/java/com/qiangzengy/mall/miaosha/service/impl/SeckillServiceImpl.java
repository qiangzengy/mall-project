package com.qiangzengy.mall.miaosha.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.qiangzengy.common.utils.R;
import com.qiangzengy.common.vo.SeckillSessionVo;
import com.qiangzengy.mall.miaosha.feign.CouponFeignService;
import com.qiangzengy.mall.miaosha.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/22
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private CouponFeignService couponFeignService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void seckillGoodsUp() {
        R lates3Day = couponFeignService.getLates3Day();
        List<SeckillSessionVo> list = lates3Day.getData("data", new TypeReference<List<SeckillSessionVo>>() {
        });
        //放入redis
        //缓存活动信息
        saveSessionfos(list);
        //缓存活动的商品信息
        saveSessionSkuInfos(list);


    }

    //TODO
    private void saveSessionSkuInfos(List<SeckillSessionVo> list) {
    }

    //TODO
    private void saveSessionfos(List<SeckillSessionVo> list) {
    }
}
