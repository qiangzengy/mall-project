package com.qiangzengy.mall.miaosha.service;

import com.qiangzengy.common.to.SessionRedisTo;

import java.util.List;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/22
 */
public interface SeckillService {

    /**
     * 扫描需要参加秒杀的商品
     */
    void seckillGoodsUp();

    List<SessionRedisTo> getCurrentSeckillSkus();
}
