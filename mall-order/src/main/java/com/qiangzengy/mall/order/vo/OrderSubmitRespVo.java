package com.qiangzengy.mall.order.vo;

import com.qiangzengy.mall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class OrderSubmitRespVo {

    private OrderEntity orderEntity;
    //错误状态码 0：成功
    private Integer code;
}
