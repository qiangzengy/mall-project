package com.qiangzengy.mall.order.dao;

import com.qiangzengy.mall.order.entity.OrderSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单配置信息
 * 
 * @author qiangzeng
 * @email ${email}
 * @date 2020-04-25 09:24:22
 */
@Mapper
public interface OrderSettingDao extends BaseMapper<OrderSettingEntity> {
	
}
