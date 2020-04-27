package com.qiangzengy.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiangzengy.common.utils.PageUtils;
import com.qiangzengy.mall.product.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author qiangzeng
 * @email qiangzengy@163.com
 * @date 2020-04-23 21:53:13
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateDetail(BrandEntity brand);
}

