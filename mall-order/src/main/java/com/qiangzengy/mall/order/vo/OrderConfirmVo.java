package com.qiangzengy.mall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 订单确认页需要用到的数据
 */
public class OrderConfirmVo {
    //收货地址
    @Getter
    @Setter
    private List<MemberAddressVo> addressVos;

    //所选购物项
    @Getter
    @Setter
    private List<OrderItemVo>itemVos;

    //积分
    @Getter
    @Setter
    private Integer intergration;

    //防重令牌
    @Getter
    @Setter
    private String orderToken;

    @Getter
    @Setter
    Map<Long,Boolean>stocks;

    //订单总额
    //private BigDecimal total;

    //应付价格
    //private BigDecimal payPrice;

    public Integer getCount(){
        Integer num=0;
        if(itemVos!=null&&itemVos.size()>0){
            for (OrderItemVo itemVo : itemVos) {
                num+=itemVo.getCount();
            }
        }
        return num;

    }


    public BigDecimal getTotal() {
        BigDecimal total =new BigDecimal("0");
        if(itemVos!=null&&itemVos.size()>0){
            for (OrderItemVo itemVo : itemVos) {
                total= total.add(itemVo.getPrice().multiply(new BigDecimal(itemVo.getCount())));
            }
        }
        return total;
    }

    public BigDecimal getPayPrice() {
        BigDecimal payPrice=new BigDecimal("0");
        if(itemVos!=null&&itemVos.size()>0){
            for (OrderItemVo itemVo : itemVos) {
                payPrice= payPrice.add(itemVo.getPrice().multiply(new BigDecimal(itemVo.getCount())));
            }
        }
        return payPrice;
    }
}
