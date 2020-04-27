package com.qiangzengy.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiangzengy.common.utils.PageUtils;
import com.qiangzengy.mall.product.entity.AttrGroupEntity;

import java.util.Map;

/**
 * 属性分组
 *
 * @author qiangzeng
 * @email qiangzengy@163.com
 * @date 2020-04-23 21:53:13
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils querycatelogIdPage(Map<String, Object> params,Long catelogId);
}

