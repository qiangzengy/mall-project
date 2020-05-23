package com.qiangzengy.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单确认页需要用到的数据
 */
@Data
public class OrderConfirmVo {
    //收货地址
    private List<MemberAddressVo> addressVos;

    //所选购物项
    private List<OrderItemVo>itemVos;

    //积分
    private Integer intergration;

    //订单总额
    private BigDecimal total;

    //应付价格
    private BigDecimal payPrice;
}
