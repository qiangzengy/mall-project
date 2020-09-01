package com.qiangzengy.mall.product.service.impl;

import com.qiangzengy.common.utils.Query;
import com.qiangzengy.mall.product.entity.vo.SkuItemSalaAttrVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiangzengy.common.utils.PageUtils;

import com.qiangzengy.mall.product.dao.SkuSaleAttrValueDao;
import com.qiangzengy.mall.product.entity.SkuSaleAttrValueEntity;
import com.qiangzengy.mall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }


    @Override
    public List<SkuItemSalaAttrVo> getSpuIdSale(Long spuId) {
        return baseMapper.getSpuIdSale(spuId);
    }


    @Override
    public List<String> getSkuSaleAttrValues(Long skuId) {
        SkuSaleAttrValueDao dao = this.baseMapper;
        List<String> strList = dao.getSkuSaleAttrValues(skuId);
        return strList;
    }
}