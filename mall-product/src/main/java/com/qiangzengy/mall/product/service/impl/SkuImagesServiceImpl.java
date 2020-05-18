package com.qiangzengy.mall.product.service.impl;

import com.qiangzengy.common.utils.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiangzengy.common.utils.PageUtils;

import com.qiangzengy.mall.product.dao.SkuImagesDao;
import com.qiangzengy.mall.product.entity.SkuImagesEntity;
import com.qiangzengy.mall.product.service.SkuImagesService;


@Service("skuImagesService")
public class SkuImagesServiceImpl extends ServiceImpl<SkuImagesDao, SkuImagesEntity> implements SkuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuImagesEntity> page = this.page(
                new Query<SkuImagesEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuImagesEntity> getSkuIdImage(Long skuId) {
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("sku_id",skuId);
        return list(queryWrapper);
    }
}