package com.qiangzengy.mall.product.entity.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class SpuBaseAttrVo {

    private String attrName;
    private List<String> attrValue;
}
