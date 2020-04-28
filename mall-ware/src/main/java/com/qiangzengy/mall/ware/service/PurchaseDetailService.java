package com.qiangzengy.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiangzengy.common.utils.PageUtils;
import com.qiangzengy.mall.ware.entity.PurchaseDetailEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author qiangzeng
 * @email ${email}
 * @date 2020-04-25 09:34:37
 */
public interface PurchaseDetailService extends IService<PurchaseDetailEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<PurchaseDetailEntity> listDetailByPurchaseId(Long id);
}

