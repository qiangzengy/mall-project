package com.qiangzengy.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiangzengy.common.utils.PageUtils;
import com.qiangzengy.mall.ware.entity.WareSkuEntity;
import com.qiangzengy.mall.ware.entity.vo.FareVo;
import com.qiangzengy.mall.ware.entity.vo.SkuHasStockVo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author qiangzeng
 * @email ${email}
 * @date 2020-04-25 09:34:37
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds);

    FareVo getFare(Long addrId);
}

