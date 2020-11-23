package com.qiangzengy.mall.coupon.service.impl;

import com.qiangzengy.mall.coupon.entity.SeckillSkuRelationEntity;
import com.qiangzengy.mall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiangzengy.common.utils.PageUtils;
import com.qiangzengy.common.utils.Query;

import com.qiangzengy.mall.coupon.dao.SeckillSessionDao;
import com.qiangzengy.mall.coupon.entity.SeckillSessionEntity;
import com.qiangzengy.mall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLates3Day() {

        LocalDate startDay=LocalDate.now();
        // 获取3天后的时间 yy:MM:dd
        LocalDate endDay = startDay.plusDays(2);
        LocalTime startTime=LocalTime.MIN;
        LocalTime endTime=LocalTime.MAX;
        LocalDateTime startOf = LocalDateTime.of(startDay, startTime);
        LocalDateTime endOf = LocalDateTime.of(endDay, endTime);
        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startOf.format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")), endOf.format(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"))));
        if (Objects.nonNull(list)){
            list.stream().map((data) -> {
                List<SeckillSkuRelationEntity> promotion_id = seckillSkuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_id", data.getId()));
                data.setSeckillSkuRelationEntities(promotion_id);
                return data;
            }).collect(Collectors.toList());
            return list;
        }
        return null;
    }
}