package com.qiangzengy.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author qiangzengy@gmail.com
 * @date 2020/11/28
 */

@Data
public class SeckillOrderTo {

    /**
     * 订单号
     */
    private String orderSn;
    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀数量
     */
    private Integer num;

    /**
     * member_id
     */
    private Long memberId;

    /**
     * 用户名
     */
    private String memberUsername;



}
