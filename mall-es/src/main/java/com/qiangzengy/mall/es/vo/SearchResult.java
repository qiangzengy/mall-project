package com.qiangzengy.mall.es.vo;

import com.qiangzengy.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

@Data
public class SearchResult {

    private List<SkuEsModel> models;//查询到的所有商品信息
    /**
     * 分页信息
     */
    private Integer pageNum;//当前页数
    private Long  total;//总条数
    private Integer totalPages;//总页数

    private List<BrandVo> brandVos;//当前查到的结果，涉及到的所有品牌
    private List<CatalogVo>catalogVos;//涉及到的所有分类
    private List<AttrVo> attrVos;//涉及到的所有属性

    @Data
    public static class BrandVo{

        private Long brandId;
        private String brandName;
        private String brandImg;

    }


    @Data
    public static class AttrVo{

        private Long attrId;
        private String attrName;
        private List<String> attrValue;

    }

    @Data
    public static class CatalogVo{

        private Long catalogId;
        private String catalogName;

    }
}
