package com.qiangzengy.mall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FareOVo {

    private MemberAddressVo addrVo;
    private BigDecimal fare;
}
