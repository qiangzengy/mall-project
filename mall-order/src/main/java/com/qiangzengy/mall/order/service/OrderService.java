package com.qiangzengy.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiangzengy.common.to.mq.SeckillOrderTo;
import com.qiangzengy.common.utils.PageUtils;
import com.qiangzengy.mall.order.entity.OrderEntity;
import com.qiangzengy.mall.order.vo.OrderConfirmVo;
import com.qiangzengy.mall.order.vo.OrderSubmitRespVo;
import com.qiangzengy.mall.order.vo.OrderSubmitVo;
import com.qiangzengy.mall.order.vo.PayVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author qiangzeng
 * @email ${email}
 * @date 2020-04-25 09:24:22
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException;

    OrderSubmitRespVo submitOrder(OrderSubmitVo orderVo);

    Integer getStatusByOrderSn(String orderSn);

    void closeOrder(OrderEntity entity);

    PayVo getOrderPay(String orderSn);

    void createSeckillOrder(SeckillOrderTo seckillOrderTo);
}

