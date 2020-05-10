package com.qiangzengy.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuEsModel {

    private long skuId;
    private long spuId;
    private String skuTitle;
    private BigDecimal skuPrice;
    private String skuImg;
    //销量
    private long saleCount;
    //是否有库存
    private Boolean hasStock;
    //热度评分
    private long hotScore;
    //品牌id
    private long brandId;
    //分类id
    private long catalogId;
    //品牌名字
    private String brandName;
    //品牌图片
    private String brandImg;
    //分类名字
    private String catalogName;
    //商品规格属性信息
    private List<Attrs>attrs;

    @Data
    public static class Attrs{
        private long attrId;
        private String attrName;
        private String attrValue;
    }


}
