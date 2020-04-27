package com.qiangzengy.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiangzengy.common.utils.PageUtils;
import com.qiangzengy.mall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author qiangzeng
 * @email qiangzengy@163.com
 * @date 2020-04-23 21:53:13
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<ProductAttrValueEntity> baseAttrlistforspu(Long spuId);
}

