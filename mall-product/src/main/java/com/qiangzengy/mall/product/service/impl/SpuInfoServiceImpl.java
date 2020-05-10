package com.qiangzengy.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.qiangzengy.common.enums.StatusEnum;
import com.qiangzengy.common.to.SkuReductionTo;
import com.qiangzengy.common.to.SpuBoundTo;
import com.qiangzengy.common.to.es.SkuEsModel;
import com.qiangzengy.common.to.es.SkuHasStockVo;
import com.qiangzengy.common.utils.Query;
import com.qiangzengy.common.utils.R;
import com.qiangzengy.mall.product.entity.*;
import com.qiangzengy.mall.product.entity.vo.*;
import com.qiangzengy.mall.product.feign.CouponFeignService;
import com.qiangzengy.mall.product.feign.SearchFeignService;
import com.qiangzengy.mall.product.feign.WareFeignService;
import com.qiangzengy.mall.product.service.*;
import com.sun.org.apache.xml.internal.resolver.CatalogEntry;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiangzengy.common.utils.PageUtils;

import com.qiangzengy.mall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Attr;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {


    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService imagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService attrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity) {
        this.baseMapper.insert(infoEntity);
    }


    @Override
    @Transactional
    public void saveSpuInfo(SpuSaveVo vo) {
        //1、保存spu基本信息 pms_spu_info
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(infoEntity);

        //2、保存Spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(infoEntity.getId());
        descEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);


        //3、保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        imagesService.saveImages(infoEntity.getId(), images);


        //4、保存spu的规格参数;pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity id = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(id.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(infoEntity.getId());

            return valueEntity;
        }).collect(Collectors.toList());
        attrValueService.saveProductAttr(collect);


        //5、保存spu的积分信息；gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存spu积分信息失败");
        }


        //5、保存当前spu对应的所有sku信息；

        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(item -> {
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                //    private String skuName;
                //    private BigDecimal price;
                //    private String skuTitle;
                //    private String skuSubtitle;
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                //5.1）、sku的基本信息；pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity -> {
                    // 没有图片路径的无需保存
                    //返回true就是需要，false就是剔除
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                //5.2）、sku的图片信息；pms_sku_image

                skuImagesService.saveBatch(imagesEntities);

                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity attrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, attrValueEntity);
                    attrValueEntity.setSkuId(skuId);

                    return attrValueEntity;
                }).collect(Collectors.toList());
                //5.3）、sku的销售属性信息：pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // //5.4）、sku的优惠、满减等信息；gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存sku优惠信息失败");
                    }
                }

            });
        }
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        String key = (String) params.get("key");
        if(!StringUtils.isEmpty(key)){
            wrapper.and((w)-> w.eq("id",key).or().like("spu_name",key));

        }
        // status=1 and (id=1 or spu_name like xxx)
        String status = (String) params.get("status");
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("publish_status",status);
        }

        String brandId = (String) params.get("brandId");
        if(!StringUtils.isEmpty(brandId)&&!"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id",brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isEmpty(catelogId)&&!"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id",catelogId);
        }

        /**
         * status: 2
         * key:
         * brandId: 9
         * catelogId: 225
         */

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }


    /**
     * 商品上架
     * @param spuId
     */
    @Override
    public void spuUp(Long spuId) {

        //查出当前spuid对应的所有sku信息
        List<SkuInfoEntity> skuInfos=skuInfoService.getSpuId(spuId);
        //查询当前sku的所有可以用来检索的规格属性
        List<ProductAttrValueEntity>attrValueEntities=attrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds=attrValueEntities.stream().map(attrValue -> attrValue.getAttrId()).collect(Collectors.toList());
        List<Long>baseAttrIds=attrService.selectSerachAttrIds(attrIds);

        Set<Long>idSet=new HashSet<>(baseAttrIds);
        //先过滤，在映射
        List<SkuEsModel.Attrs>attrsList=attrValueEntities.stream().filter(item -> {
            //判断set是否包括attrId
            return idSet.contains(item.getAttrId());

        }).map(item -> {

            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item,attrs);
            return attrs;
        }).collect(Collectors.toList());

        //查询所有的skuId
        List<Long> skuIdList=skuInfos.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        //此处需要注意，可能会由于网络的原因，导致调用远程服务失败，可以try一下
        Map<Long,Boolean>map=null;
        try {

            //发送远程调用库存服务，查询是否有库存 （asStock 是否有库存）
            R<List<SkuHasStockVo>> hasStock=wareFeignService.getSkuHasStock(skuIdList);
            List<SkuHasStockVo> hasStockVoList=hasStock.getData();
            map=hasStockVoList.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId,item -> item.getHasStock()));

        }catch (Exception e){
            log.error("调用库存服务失败:原因{}",e);
        }


        //封装每个sku的信息
        Map<Long, Boolean> finalMap = map;
        List<SkuEsModel> collect = skuInfos.stream().map(sku -> {
            //组装需要的数据
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, esModel);
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());
            if (finalMap != null) {
                //查看是否有库存
                esModel.setHasStock(finalMap.get(sku.getSkuId()));
            } else {
                esModel.setHasStock(true);
            }

            //TODO 需要完善（hotScore 热度评分）
            esModel.setHotScore(0);
            BrandEntity entity = brandService.getById(sku.getBrandId());
            esModel.setBrandName(entity.getName());
            esModel.setBrandImg(entity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            esModel.setCatalogName(categoryEntity.getName());
            esModel.setAttrs(attrsList);

            return esModel;
        }).collect(Collectors.toList());

        //TODO 4.将数据发送给es进行保存
        R r = searchFeignService.productStatusUp(collect);
        if(r.getCode()==0){
            //修改spu状态，上架
            baseMapper.uodataStatus(spuId, StatusEnum.SPU_UP.getCode());
        }else {
            //重复调用的问题，接口幂等性，重试机制
        }

    }
}