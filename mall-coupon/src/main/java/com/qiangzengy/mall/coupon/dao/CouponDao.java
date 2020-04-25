package com.qiangzengy.mall.coupon.dao;

import com.qiangzengy.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author qiangzeng
 * @email ${email}
 * @date 2020-04-25 09:40:31
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
