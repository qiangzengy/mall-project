package com.qiangzengy.mall.ware.entity.vo;

import io.swagger.models.auth.In;
import lombok.Data;

import java.util.List;

@Data
public class SkuWareHasStock {
    private Long skuId;
    private Integer num;
    private List<Long>wareId;

}
