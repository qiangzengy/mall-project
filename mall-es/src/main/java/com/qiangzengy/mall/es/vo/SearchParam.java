package com.qiangzengy.mall.es.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParam {

    private String keyword;//页面传递过来的全文匹配关键字
    private Long catelog3Id;//三级分类id
    private String sort;//排序，价格、热度、销量
    private Integer hasStock=1;// 是否显示有货 0：无货，1：有货
    private String skuPrice;// 价格区间
    private List<Long> brandId; //品牌,允许多选
    private List<String>attrs; //属性
    private Integer pageNum=1; //分页

    private String queryUrl;


}
