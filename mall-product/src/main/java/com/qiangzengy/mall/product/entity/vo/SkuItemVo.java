package com.qiangzengy.mall.product.entity.vo;

import com.qiangzengy.mall.product.entity.SkuImagesEntity;
import com.qiangzengy.mall.product.entity.SkuInfoEntity;
import com.qiangzengy.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {


    //1。sku的基本信息 pms_sku_info
    private SkuInfoEntity info;
    //2。sku的图片信息 pms_sku_images
    private List<SkuImagesEntity> images;
    //3。spu的销售属性
    List<SkuItemSalaAttrVo> salaVos;
    //4。spu介绍 pms_spu_info_desc
    private SpuInfoDescEntity desc;
    //5。spu规格参数
    private List<SpuItemAttrGroupVo> groupVos;


}

