package com.qiangzengy.mall.miaosha.controller;

import com.qiangzengy.common.to.SessionRedisTo;
import com.qiangzengy.common.utils.R;
import com.qiangzengy.mall.miaosha.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/24
 */

@RestController
@RequestMapping("/seckill")
public class SeckillContriller {

    @Autowired
    private SeckillService seckillService;

    // 查询当前秒杀商品信息
    @GetMapping("/current/skus")
    public R getCurrentSeckillSkus(){
        List<SessionRedisTo> to = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(to);
    }

    // 查询秒杀商品信息
    @GetMapping("/current/skus/{skuId}")
    public R getSeckillSkus(@PathVariable("skuId") Long skuId){
        SessionRedisTo to = seckillService.getSeckillSkus(skuId);
        return R.ok().setData(to);
    }
}
