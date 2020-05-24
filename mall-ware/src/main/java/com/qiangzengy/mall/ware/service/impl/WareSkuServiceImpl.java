package com.qiangzengy.mall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.qiangzengy.common.utils.R;
import com.qiangzengy.mall.ware.entity.vo.*;
import com.qiangzengy.mall.ware.exception.NoStockException;
import com.qiangzengy.mall.ware.feign.MemberFeignService;
import com.qiangzengy.mall.ware.feign.ProductFeignService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiangzengy.common.utils.PageUtils;
import com.qiangzengy.common.utils.Query;

import com.qiangzengy.mall.ware.dao.WareSkuDao;
import com.qiangzengy.mall.ware.entity.WareSkuEntity;
import com.qiangzengy.mall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private WareSkuDao wareSkuDao;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private MemberFeignService memberFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * skuId: 1
         * wareId: 2
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if(!StringUtils.isEmpty(skuId)){
            queryWrapper.eq("sku_id",skuId);
        }

        String wareId = (String) params.get("wareId");
        if(!StringUtils.isEmpty(wareId)){
            queryWrapper.eq("ware_id",wareId);
        }


        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }


    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        //1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> entities = wareSkuDao.selectList(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if(entities == null || entities.size() == 0){
            WareSkuEntity skuEntity = new WareSkuEntity();
            skuEntity.setSkuId(skuId);
            skuEntity.setStock(skuNum);
            skuEntity.setWareId(wareId);
            skuEntity.setStockLocked(0);
            //远程查询sku的名字，如果失败，整个事务无需回滚，自己catch异常
            try {
                R info = productFeignService.info(skuId);
                Map<String,Object> data = (Map<String, Object>) info.get("skuInfo");

                if(info.getCode() == 0){
                    skuEntity.setSkuName((String) data.get("skuName"));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            wareSkuDao.insert(skuEntity);
        }else{
            wareSkuDao.addStock(skuId,wareId,skuNum);
        }
    }


    @Override
    public List<SkuHasStockVo> getSkuHasStock(List<Long> skuIds) {
        List<SkuHasStockVo>stockVos= skuIds.stream().map(skuId -> {
            SkuHasStockVo stockVo=new SkuHasStockVo();
            stockVo.setSkuId(skuId);
            Long count=wareSkuDao.getSkuHasStock(skuId);
            stockVo.setHasStock(count==null?false:count>0);
            return stockVo;
        }).collect(Collectors.toList());
        return stockVos;
    }


    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo=new FareVo();
        R info = memberFeignService.info(addrId);
        MemberAddrVo address = info.getData("memberReceiveAddress", new TypeReference<MemberAddrVo>() {
        });
        fareVo.setAddrVo(address);
        if (address!=null){
            //TODO 需要调用第三方物流接口，待完善
            //模拟运费
            Integer num=new Random().nextInt(6) + 5;
            fareVo.setFare(new BigDecimal(num+""));
        }
        return fareVo;
    }


    /**
     *
     * (rollbackFor = NoStockException.class)
     * 默认出现异常都回滚
     * 锁库存
     * @param lockVo
     * @return
     */
    @Override
    @Transactional
    public Boolean orderLockStock(WareSkuLockVo lockVo) {

        //找到每个商品在哪个仓库有库存
        List<OrderItemVo> itemLocks = lockVo.getItemLocks();
        List<SkuWareHasStock> collect = itemLocks.stream().map(item -> {
            SkuWareHasStock hasStock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            hasStock.setSkuId(skuId);
            //1.查找仓库库存，根据skuId
            List<Long> wareIds=wareSkuDao.listSkuId(skuId);
            hasStock.setWareId(wareIds);
            hasStock.setNum(item.getCount());
            return hasStock;
        }).collect(Collectors.toList());
        //2.锁定库存
        for (SkuWareHasStock hasStock : collect) {
            Boolean stoked=false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds!=null){
                for (Long wareId : wareIds) {
                   Long count= wareSkuDao.lockStock(skuId,wareId,hasStock.getNum());
                   if (count==1){
                       stoked=true;
                       //锁成功了，不需要锁后面的仓库，break跳出当前循环
                       break;
                   }
                }
                //stoked=false,说明当前商品库存没锁住
                if(!stoked){
                    throw new NoStockException(skuId);
                }
            }else {
                throw new NoStockException(skuId);
            }
        }
        return true;
    }
}