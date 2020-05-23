package com.qiangzengy.mall.order.service.impl;

import com.qiangzengy.mall.order.feign.MemberFeignService;
import com.qiangzengy.mall.order.interceptor.LoginUserInterceptor;
import com.qiangzengy.mall.order.vo.MemberAddressVo;
import com.qiangzengy.mall.order.vo.MemberResVo;
import com.qiangzengy.mall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiangzengy.common.utils.PageUtils;
import com.qiangzengy.common.utils.Query;

import com.qiangzengy.mall.order.dao.OrderDao;
import com.qiangzengy.mall.order.entity.OrderEntity;
import com.qiangzengy.mall.order.service.OrderService;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }


    /**
     * 订单确认页需要用到的数据
     * @return
     */
    @Override
    public OrderConfirmVo confirmOrder() {

        OrderConfirmVo confirmVo=new OrderConfirmVo();
        //从登陆拦截器获取当前用户
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        List<MemberAddressVo> addressVos = memberFeignService.getByMemberId(memberResVo.getId());
        confirmVo.setAddressVos(addressVos);

        //远程查询购物项
        // TODO 需要从购物车模块查询，待完善

        return null;
    }
}