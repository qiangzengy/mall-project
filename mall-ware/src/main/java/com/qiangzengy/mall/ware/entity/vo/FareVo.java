package com.qiangzengy.mall.ware.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FareVo {

    private MemberAddrVo addrVo;
    private BigDecimal fare;
}
