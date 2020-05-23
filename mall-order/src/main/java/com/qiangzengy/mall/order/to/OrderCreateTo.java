package com.qiangzengy.mall.order.to;

import com.qiangzengy.mall.order.entity.OrderEntity;
import com.qiangzengy.mall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {

    private OrderEntity orderEntity;
    //创建的订单项
    private List<OrderItemEntity> items;
    //订单应付价格
    private BigDecimal payPrice;
    //运费
    private BigDecimal fare;

}
