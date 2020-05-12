package com.qiangzengy.mall.product.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catalog2Vo {

    //1级父分类
    private String catalog1Id;
    //三级子分类
    private List<Catalog3Vo> catalog3List;
    private String id;
    private String name;


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Catalog3Vo{

        private String catalog2Id;
        private String id;
        private String name;

    }
}
