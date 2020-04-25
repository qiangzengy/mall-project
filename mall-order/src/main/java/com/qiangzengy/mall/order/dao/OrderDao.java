package com.qiangzengy.mall.order.dao;

import com.qiangzengy.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author qiangzeng
 * @email ${email}
 * @date 2020-04-25 09:24:22
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
