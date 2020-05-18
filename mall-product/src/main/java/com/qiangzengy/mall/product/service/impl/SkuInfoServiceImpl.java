package com.qiangzengy.mall.product.service.impl;

import com.qiangzengy.common.utils.Query;
import com.qiangzengy.mall.product.entity.SkuImagesEntity;
import com.qiangzengy.mall.product.entity.SpuInfoDescEntity;
import com.qiangzengy.mall.product.entity.vo.SkuItemSalaAttrVo;
import com.qiangzengy.mall.product.entity.vo.SkuItemVo;
import com.qiangzengy.mall.product.entity.vo.SpuItemAttrGroupVo;
import com.qiangzengy.mall.product.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiangzengy.common.utils.PageUtils;

import com.qiangzengy.mall.product.dao.SkuInfoDao;
import com.qiangzengy.mall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService imagesService;

    @Autowired
    private SpuInfoDescService descService;

    @Autowired
    private AttrGroupService groupService;

    @Autowired
    private SkuSaleAttrValueService saleAttrValueService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<>()
        );
        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            queryWrapper.and((wrapper)-> wrapper.eq("sku_id",key).or().like("sku_name",key));
        }

        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId)&&!"0".equalsIgnoreCase(catelogId)){
            queryWrapper.eq("catalog_id",catelogId);
        }

        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId)&&!"0".equalsIgnoreCase(catelogId)){
            queryWrapper.eq("brand_id",brandId);
        }

        String min = (String) params.get("min");
        if(!StringUtils.isEmpty(min)){
            queryWrapper.ge("price",min);
        }

        String max = (String) params.get("max");

        if(!StringUtils.isEmpty(max)  ){
            try{
                BigDecimal bigDecimal = new BigDecimal(max);

                if(bigDecimal.compareTo(new BigDecimal("0"))==1){
                    queryWrapper.le("price",max);
                }
            }catch (Exception e){

            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSpuId(long spuId) {
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id",spuId);
        return this.list(queryWrapper);
     }


    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {

        SkuItemVo itemVo=new SkuItemVo();

        /**
         * 异步编排，3、4、5需要依赖1的结果
         */

        //该结果，别人还要用
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1。sku的基本信息 pms_sku_info
            SkuInfoEntity info = getById(skuId);
            itemVo.setInfo(info);
            return info;
        }, executor);
        //Long spuId=info.getSpuId();
        //Long catalogId = info.getCatalogId();

        CompletableFuture<Void> salaVosFuture = infoFuture.thenAcceptAsync((info) -> {
            //3。spu的销售属性
            List<SkuItemSalaAttrVo> salaVos = saleAttrValueService.getSpuIdSale(info.getSpuId());
            itemVo.setSalaVos(salaVos);
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((info) -> {
            //4。spu介绍
            SpuInfoDescEntity desc = descService.getById(info.getSpuId());
            itemVo.setDesc(desc);
        }, executor);

        CompletableFuture<Void> groupVosFuture = infoFuture.thenAcceptAsync(info -> {
            //5。spu规格参数
            List<SpuItemAttrGroupVo> groupVos = groupService.getAttrGroupWithAttrsBySpuId(info.getSpuId(), info.getCatalogId());
            itemVo.setGroupVos(groupVos);
        }, executor);

        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            //2。sku的图片信息 pms_sku_images
            List<SkuImagesEntity> images = imagesService.getSkuIdImage(skuId);
            itemVo.setImages(images);
        }, executor);

        //等待所有任务都完成
        CompletableFuture.allOf(descFuture,groupVosFuture,salaVosFuture,imagesFuture).get();
        return itemVo;
    }

}