package com.qiangzengy.mall.ware.entity.vo;

import lombok.Data;

@Data
public class LockStockResult {


    private Long skuId;
    //锁几件
    private Integer num;
    //是否锁成功
    private Boolean locked;
}
