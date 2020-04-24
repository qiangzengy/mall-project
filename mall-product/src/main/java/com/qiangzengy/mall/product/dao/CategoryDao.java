package com.qiangzengy.mall.product.dao;

import com.qiangzengy.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author qiangzeng
 * @email qiangzengy@163.com
 * @date 2020-04-23 21:53:13
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
