package com.qiangzengy.mall.ware.dao;

import com.qiangzengy.mall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author qiangzeng
 * @email ${email}
 * @date 2020-04-25 09:34:37
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Long getSkuHasStock(@Param("skuId") Long skuId);

    List<Long> listSkuId(@Param("skuId") Long skuId);

    Long lockStock(@Param("skuId")Long skuId, @Param("wareId")Long wareId, @Param("num")Integer num);

    Long unLockStock(@Param("skuId")Long skuId, @Param("wareId")Long wareId, @Param("num")Integer skuNum);
}
