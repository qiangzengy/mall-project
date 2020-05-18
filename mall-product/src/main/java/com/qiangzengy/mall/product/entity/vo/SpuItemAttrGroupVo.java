package com.qiangzengy.mall.product.entity.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<SpuBaseAttrVo> baseAttrVos;
}
