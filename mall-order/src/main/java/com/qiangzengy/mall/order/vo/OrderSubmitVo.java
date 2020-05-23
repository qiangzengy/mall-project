package com.qiangzengy.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单提交数据
 */
@Data
public class OrderSubmitVo {
    //收货地址的id
    private Long addrId;
    //支付方式
    private Integer payType;
    //无需提交需要购买的商品，去购物车重新获取一次
    //优惠、发票
    //防重令牌
    private String orderToken;
    //应付价格
    private BigDecimal payPrice;
    //用户信息，可以在session中获取
    //备注信息
    private String note;
}
