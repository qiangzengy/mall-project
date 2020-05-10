package com.qiangzengy.mall.product.dao;

import com.qiangzengy.mall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 * 
 * @author qiangzeng
 * @email qiangzengy@163.com
 * @date 2020-04-23 21:53:12
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    void uodataStatus(@Param("spuId") Long spuId, @Param("code") int code);
}
