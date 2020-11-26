package com.qiangzengy.mall.miaosha.controller;

import com.qiangzengy.common.to.SessionRedisTo;
import com.qiangzengy.common.utils.R;
import com.qiangzengy.mall.miaosha.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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

    //TODO 查询秒杀商品信息
    @GetMapping("/current/skus")
    public R getCurrentSeckillSkus(){
        List<SessionRedisTo> to = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(to);
    }
}
