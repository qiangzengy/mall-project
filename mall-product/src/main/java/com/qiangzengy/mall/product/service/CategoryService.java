package com.qiangzengy.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiangzengy.common.utils.PageUtils;
import com.qiangzengy.mall.product.entity.CategoryEntity;
import com.qiangzengy.mall.product.entity.vo.Catalog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author qiangzeng
 * @email qiangzengy@163.com
 * @date 2020-04-23 21:53:13
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> treeList();

    void removeCategory(List<Long> ids);

    Long[] findCatelogPath(Long catelogId);

    List<CategoryEntity> getLevel1Category();

    Map<String, List<Catalog2Vo>> getCatalogJson();

    void updateCascade(CategoryEntity category);

    Map<String, List<Catalog2Vo>> getCatalogJsonFromDBDb();


}

