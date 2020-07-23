package com.qiangzengy.mall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.qiangzengy.common.to.mq.OrderEntityTo;
import com.qiangzengy.common.to.mq.StockDetailTo;
import com.qiangzengy.common.to.mq.StockLockTo;
import com.qiangzengy.common.utils.R;
import com.qiangzengy.mall.ware.entity.WareOrderTaskDetailEntity;
import com.qiangzengy.mall.ware.entity.WareOrderTaskEntity;
import com.qiangzengy.mall.ware.entity.vo.*;
import com.qiangzengy.mall.ware.exception.NoStockException;
import com.qiangzengy.mall.ware.feign.MemberFeignService;
import com.qiangzengy.mall.ware.feign.OderFeignService;
import com.qiangzengy.mall.ware.feign.ProductFeignService;
import com.qiangzengy.mall.ware.service.WareOrderTaskService;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
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

import javax.annotation.Resource;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Resource
    private WareSkuDao wareSkuDao;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private WareOrderTaskService wareOrderTaskService;

    @Autowired
    private WareOrderTaskDetailServiceImpl wareOrderTaskDetailService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private OderFeignService oderFeignService;

    @Override
    public void unLockStock(StockLockTo to){

        StockDetailTo detail = to.getDetail();
        Long detailId = detail.getId();
        /**
         * 解锁：
         * 1。查询数据库关于这个订单的锁定库存信息。
         * 没有：库存锁定失败，库存回滚了，就无需解锁
         * 有：证明库存锁定成功了
         *     1。没有这个订单，就必须解锁
         *     2。有这个订单，需要判断订单状，已取消，需要解锁库存
         */
        WareOrderTaskDetailEntity byId = wareOrderTaskDetailService.getById(detailId);
        if(byId!=null){
            WareOrderTaskEntity taskEntity = wareOrderTaskService.getById(byId.getTaskId());
            String orderSn = taskEntity.getOrderSn();
            R r = oderFeignService.getStatusByOrderSn(orderSn);
            if (r.getCode()==0){
                Integer orderStatus = r.getData("orderStatus", new TypeReference<Integer>() {
                });
                if(orderStatus==null||orderStatus==4){
                    //只有库存的状态为已锁定，才可以解锁
                    if(byId.getLock_status()==1){
                        //订单已取消，解锁库存
                        wareSkuDao.unLockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum());
                        //更新库存工作单详情的状态

                        WareOrderTaskDetailEntity entity = wareOrderTaskDetailService.getById(detailId);
                        entity.setId(detailId);
                        entity.setLock_status(2);
                        wareOrderTaskDetailService.updateById(entity);
                    }
                }
            }else {
                throw new RuntimeException("远程服务失败");
            }
        }
    }


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

        /**
         * 保存库存工作单的详情
         */
        WareOrderTaskEntity taskEntity=new WareOrderTaskEntity();
        taskEntity.setOrderSn(lockVo.getOrderSn());
        wareOrderTaskService.save(taskEntity);

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
                       //在库存工作单详情里面插入数据
                       WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
                       detailEntity.setSkuId(skuId);
                       detailEntity.setSkuNum(hasStock.getNum());
                       detailEntity.setWareId(wareId);
                       detailEntity.setTaskId(taskEntity.getId());
                       detailEntity.setLock_status(1);
                       wareOrderTaskDetailService.save(detailEntity);
                       //告诉MQ库存锁定成功
                       StockLockTo stockLockTo = new StockLockTo();
                       StockDetailTo detail=new StockDetailTo();
                       stockLockTo.setId(taskEntity.getId());
                       BeanUtils.copyProperties(detailEntity,detail);
                       stockLockTo.setDetail(detail);
                       rabbitTemplate.convertAndSend("stock-event-exchange","stock.locked",stockLockTo);

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


    @Override
    @Transactional
    public void unLockStock(OrderEntityTo orderTo) {

        String orderSn = orderTo.getOrderSn();
        WareOrderTaskEntity entity=wareOrderTaskService.getTaskByOrderSn(orderSn);
        Long taskId = entity.getId();
        List<WareOrderTaskDetailEntity> list = wareOrderTaskDetailService.list(new QueryWrapper<WareOrderTaskDetailEntity>().eq("task_id", taskId).eq("lock_status", 1));
        for (WareOrderTaskDetailEntity detail : list) {
            wareSkuDao.unLockStock(detail.getSkuId(),detail.getWareId(),detail.getSkuNum());
            //更新库存工作单详情的状态
            WareOrderTaskDetailEntity dentity = wareOrderTaskDetailService.getById(detail.getId());
            dentity.setId(detail.getId());
            dentity.setLock_status(2);
            wareOrderTaskDetailService.updateById(dentity);
        }

    }
}