package com.qiangzengy.mall.order.dao;

import com.qiangzengy.mall.order.entity.OrderItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单项信息
 * 
 * @author qiangzeng
 * @email ${email}
 * @date 2020-04-25 09:24:22
 */
@Mapper
public interface OrderItemDao extends BaseMapper<OrderItemEntity> {
	
}
