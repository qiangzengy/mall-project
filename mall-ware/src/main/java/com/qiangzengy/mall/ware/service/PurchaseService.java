package com.qiangzengy.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiangzengy.common.utils.PageUtils;
import com.qiangzengy.mall.ware.entity.PurchaseEntity;
import com.qiangzengy.mall.ware.entity.vo.MergeVo;
import com.qiangzengy.mall.ware.entity.vo.PurchaseDoneVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author qiangzeng
 * @email ${email}
 * @date 2020-04-25 09:34:37
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);

    void done(PurchaseDoneVo doneVo);

    void received(List<Long> ids);

    void mergePurchase(MergeVo mergeVo);
}

