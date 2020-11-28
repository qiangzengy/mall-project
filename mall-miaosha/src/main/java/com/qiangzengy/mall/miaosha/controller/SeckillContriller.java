package com.qiangzengy.mall.miaosha.controller;

import com.qiangzengy.common.to.SessionRedisTo;
import com.qiangzengy.common.utils.R;
import com.qiangzengy.mall.miaosha.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 查询当前秒杀商品信息
     * @return
     */
    @GetMapping("/current/skus")
    public R getCurrentSeckillSkus(){
        List<SessionRedisTo> to = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(to);
    }

    /**
     * 查询当前商品秒杀信息
     * @param skuId
     * @return
     */
    @GetMapping("/current/skus/{skuId}")
    public R getSeckillSkus(@PathVariable("skuId") Long skuId){
        SessionRedisTo to = seckillService.getSeckillSkus(skuId);
        return R.ok().setData(to);
    }

    /**
     * 购买秒杀商品
     * @param seckillId
     * @param key 随机码
     * @param num 秒杀数量
     * @return
     */
    @GetMapping("/buy/skus")
    public R buySeckillSkus(@RequestParam("seckillId") String seckillId,@RequestParam("key") String key,@RequestParam("num") Integer num){
        String orderSn=seckillService.buySeckillSkus(seckillId,key,num);
        return R.ok().setData(orderSn);
    }


}
