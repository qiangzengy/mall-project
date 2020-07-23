package com.qiangzengy.mall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.qiangzengy.common.constant.OrderConstant;
import com.qiangzengy.common.exception.NoStockException;
import com.qiangzengy.common.to.mq.OrderEntityTo;
import com.qiangzengy.common.utils.R;
import com.qiangzengy.mall.order.entity.OrderItemEntity;
import com.qiangzengy.mall.order.enume.OrderStatusEnum;
import com.qiangzengy.mall.order.feign.CartFeignService;
import com.qiangzengy.mall.order.feign.MemberFeignService;
import com.qiangzengy.mall.order.feign.ProductFeignService;
import com.qiangzengy.mall.order.feign.WmsFeignService;
import com.qiangzengy.mall.order.interceptor.LoginUserInterceptor;
import com.qiangzengy.mall.order.service.OrderItemService;
import com.qiangzengy.mall.order.to.OrderCreateTo;
import com.qiangzengy.mall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiangzengy.common.utils.PageUtils;
import com.qiangzengy.common.utils.Query;

import com.qiangzengy.mall.order.dao.OrderDao;
import com.qiangzengy.mall.order.entity.OrderEntity;
import com.qiangzengy.mall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


/**
 * seata AT模式控制分布式事务：（不适合高并发）
 * )、每一个微服务先必须创建 undo_log
 * 2)、安装事务协调器seata-server:httpsi//github.com/seata/seata/releases
 * 3)、整合
 * 1、导入依赖 spring-cLoud-starter-alibaba-seata seata-all-0.7.1
 * 2、解压并启动 seata-server;
 * registry.conf:注册中心配置;修改 registry type=nacos
 * file.conf
 * 3、所有想要用到分布式事务的微服务使用 seata DataSourceProxy代理自己的数据源
 * 4、每个数服务,都必须导入
 *     registry. conf
 *     file.conf vgroup_mapping.gulimall-ware-fescar-service-group ="default"
 * 5,启动测试
 * 6、给分布式大事务的入口标注 @GlobalTransactional
 * 7、每一个远程的小事务用    @Transactional
 */

@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements  OrderService {

    private ThreadLocal<OrderSubmitVo>submitVo=new ThreadLocal<>();

    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Autowired
    private WmsFeignService wmsFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

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
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {

        OrderConfirmVo confirmVo=new OrderConfirmVo();
        //从登陆拦截器获取当前用户
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        /**
         * 这里使用异步编排，会出现丢失上下文的问题，因为不是同一个线程执行任务
         * 解决：1。getRequestAttributes 先获取请求信息
         *      2。setRequestAttributes 将请求信息set到异步编排的线程里
         */
        //获取之前的请求
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> addFuture = CompletableFuture.runAsync(() -> {
            //每一个线程都共享之前的数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //远程查询地址列表
            List<MemberAddressVo> addressVos = memberFeignService.getByMemberId(memberResVo.getId());
            confirmVo.setAddressVos(addressVos);
        }, executor);

        CompletableFuture<Void> itemsFuture = CompletableFuture.runAsync(() -> {
            //每一个线程都共享之前的数据
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //远程查询购物项
            List<OrderItemVo> items = cartFeignService.getCurrentUserCartItems();
            confirmVo.setItemVos(items);
        }, executor).thenRunAsync(()->{
            List<OrderItemVo> itemVos = confirmVo.getItemVos();
            //查询所有商品的id
            List<Long> ids = itemVos.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
            R r = wmsFeignService.getSkuHasStock(ids);
            List<SkuStockVo> data = r.getData("data", new TypeReference<List<SkuStockVo>>() {
            });
            if (data!=null){
                Map<Long, Boolean> map = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                confirmVo.setStocks(map);
            }

        },executor);

        //查询积分
        confirmVo.setIntergration(memberResVo.getIntegration());

        //防重令牌
        String token= UUID.randomUUID().toString().replace("_","");

        //redis存入一个
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+memberResVo.getId(),token,30, TimeUnit.MINUTES);

        //页面一个
        confirmVo.setOrderToken(token);
        CompletableFuture.allOf(addFuture,itemsFuture).get();
        return confirmVo;
    }

    @Override
    @Transactional
    //@GlobalTransactional// seata分布式事务
    public OrderSubmitRespVo submitOrder(OrderSubmitVo orderVo) {
        submitVo.set(orderVo);
        OrderSubmitRespVo respVo=new OrderSubmitRespVo();
        respVo.setCode(0);
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        //下单：验证令牌->创建订单->校验价格->锁库存

        //验证令牌，原子性
        /**
         * 如果redis调用get方法来获取一个key的值，这get出来的值等于我们传过来的值，
         * 然后它会执行，把值删除，删除成功返回1，否则返回0
         */
        String script="if redis.call('get', KEYS[1])==ARGV[1]then return redis.call('del', KEYS[1])else return O end";
        String pageToken=orderVo.getOrderToken();
        Long execute = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResVo.getId()), pageToken);
        if (execute==1){
            //令牌验证成功
            OrderCreateTo order = createOrder();
            //验价
            BigDecimal payAmount = order.getOrderEntity().getPayAmount();
            BigDecimal payPrice = orderVo.getPayPrice();
            if (Math.abs(payPrice.subtract(payAmount).doubleValue())<0.01){
                //保存订单
                saveOrder(order);
                //库存锁定，只要异常回滚订单数据
                WareSkuLockVo lockVo=new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrderEntity().getOrderSn());
                List<OrderItemVo> collect = order.getItems().stream().map(item -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(item.getSkuId());
                    itemVo.setCount(item.getSkuQuantity());
                    itemVo.setTitle(item.getSkuName());
                    return itemVo;
                }).collect(Collectors.toList());
                lockVo.setItemLocks(collect);

                /**
                 * 1。为了保证高并发，库存服务自己回滚，可以发消息给库存服务
                 * 2。库存服务本身可以使用自动解锁模式，采用消息队列
                 */

                R r = wmsFeignService.orderLockStock(lockVo);
                if (r.getCode()==0){
                    //锁成功
                    //订单创建成功，给MQ发送消息
                    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",order.getOrderEntity());
                    return respVo;
                }else {
                    //锁失败
                    throw new NoStockException();
                }

            }else {
                respVo.setCode(2);
                return respVo;
            }
        }
        respVo.setCode(1);
        return respVo;
    }

    private void saveOrder(OrderCreateTo order) {

        OrderEntity orderEntity = order.getOrderEntity();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);
        List<OrderItemEntity> items = order.getItems();
        orderItemService.saveBatch(items);
    }

    /**
     * 创建订单
     */
    private OrderCreateTo createOrder(){

        OrderCreateTo createTo = new OrderCreateTo();

        String orderSn = IdWorker.getTimeId();

        //构建收货的订单数据
        OrderEntity orderEntity = buildOrder(orderSn);

        //获取所有订单项信息
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

        //计算价格
        computePrice(orderEntity,orderItemEntities);

        createTo.setOrderEntity(orderEntity);
        createTo.setItems(orderItemEntities);
        return createTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {

        //订单的价格
        BigDecimal totalP=new BigDecimal("0");
        //优惠的金额
        BigDecimal compu=new BigDecimal("0");
        BigDecimal integ=new BigDecimal("0");
        BigDecimal promo=new BigDecimal("0");
        Integer gigtG=0;
        Integer gigtI=0;


        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            totalP=totalP.add(orderItemEntity.getSkuPrice());
            compu=compu.add(orderItemEntity.getCouponAmount());
            integ=integ.add(orderItemEntity.getIntegrationAmount());
            promo=promo.add(orderItemEntity.getPromotionAmount());
            gigtG=gigtG+orderItemEntity.getGiftGrowth();
            gigtI=gigtI+orderItemEntity.getGiftIntegration();

        }
        //订单总额
        orderEntity.setTotalAmount(totalP);
        //应付总额
        orderEntity.setPayAmount(totalP.add(orderEntity.getFreightAmount()));
        //优惠总额
        orderEntity.setPromotionAmount(promo);
        orderEntity.setIntegrationAmount(integ);
        orderEntity.setCouponAmount(compu);
        orderEntity.setGrowth(gigtG);
        orderEntity.setIntegration(gigtI);
        orderEntity.setDeleteStatus(0);

    }

    private OrderEntity buildOrder(String orderSn) {
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        OrderEntity entity=new OrderEntity();
        entity.setMemberId(memberResVo.getId() );
        //生成订单号
        entity.setOrderSn(orderSn);
        //获取收货地址信息
        OrderSubmitVo orderSubmitVo = submitVo.get();
        R fare = wmsFeignService.getFare(orderSubmitVo.getAddrId());
        FareOVo fareVo = fare.getData("fareVo", new TypeReference<FareOVo>() {
        });
        entity.setReceiverDetailAddress(fareVo.getAddrVo().getDetailAddress());
        entity.setReceiverCity(fareVo.getAddrVo().getCity());
        entity.setReceiverProvince(fareVo.getAddrVo().getProvince());
        entity.setReceiverRegion(fareVo.getAddrVo().getRegion());
        //收货人名
        entity.setReceiverName(fareVo.getAddrVo().getName());
        entity.setReceiverPhone(fareVo.getAddrVo().getPhone());
        //获取运费
        entity.setFreightAmount(fareVo.getFare());
        //订单状态
        entity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        //自动确收时间
        entity.setAutoConfirmDay(15);
        return entity;
    }

    /**
     * 构建所有订单项数据
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {

        List<OrderItemVo> cartItems = cartFeignService.getCurrentUserCartItems();
        if(cartItems!=null&&cartItems.size()>0){
            List<OrderItemEntity> collect = cartItems.stream().map(item -> {
                OrderItemEntity itemEntity = buildOrderItem(item);
                itemEntity.setOrderSn(orderSn);
                return itemEntity;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    /**
     * 构建某一个订单项
     * @param item
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo item) {

        OrderItemEntity entity = new OrderItemEntity();
        //spu信息
        Long skuId=item.getSkuId();
        R r = productFeignService.getSpuInfoBySkuId(skuId);
        SpuInfoVo spuInfoVo = r.getData("data", new TypeReference<SpuInfoVo>() {
        });
        entity.setSpuId(spuInfoVo.getId());
        entity.setSpuBrand(spuInfoVo.getBrandId().toString());
        entity.setSpuName(spuInfoVo.getSpuName());
        entity.setCategoryId(spuInfoVo.getCatalogId());
        //sku信息
        entity.setSkuId(skuId);
        entity.setSkuName(item.getTitle());
        entity.setSkuPic(item.getImage());
        entity.setSkuQuantity(item.getCount());
        entity.setSkuPrice(item.getPrice());
        String attrs = StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";");
        entity.setSkuAttrsVals(attrs);
        //优惠信息
        //积分信息
        entity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        entity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount().toString())).intValue());
        //促销价格
        entity.setPromotionAmount(new BigDecimal("0"));
        //优惠卷
         entity.setCouponAmount(new BigDecimal("0"));
         //积分优惠
        entity.setIntegrationAmount(new BigDecimal("0"));
        //实际价格
        entity.setRealAmount(entity.getSkuPrice().multiply(new BigDecimal(entity.getSkuQuantity().toString()))
                .subtract(entity.getCouponAmount()).subtract(entity.getIntegrationAmount())
                .subtract(entity.getPromotionAmount()));

        return entity;
    }


    @Override
    public Integer getStatusByOrderSn(String orderSn) {
        OrderEntity entity = baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return entity.getStatus();
    }


    /**
     * 过期订单的关闭
     * @param entity
     */
    @Override
    public void closeOrder(OrderEntity entity) {

        //1.查询订单的状态,待付款，才可以关闭
        OrderEntity orderEntity = baseMapper.selectById(entity.getId());
        if(orderEntity.getStatus()==OrderStatusEnum.CREATE_NEW.getCode()){

            OrderEntity update = new OrderEntity();
            update.setId(entity.getId());
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            update.setModifyTime(new Date());
            updateById(update);
            /**
             * 此时上面可能存在问题：关闭订单的操作在解锁库存的后面完成，此时就是导致订单关闭，
             * 库存没有解锁
             * 解决：在发送一个消息给库存MQ
             * 流程图：https://www.yuque.com/qiangzeng/giut9f/mmlg79
             */

            OrderEntityTo orderTo=new OrderEntityTo();
            BeanUtils.copyProperties(orderEntity,orderTo);

            /**
             * 保证消息100%发出去
             * 1。网络异常的解决：
             * 每一个消息可以做好日志记录，可以在数据库创建一个MQ消息表，
             * 定时扫描数据库，将失败的消息重发一遍
             * 2。消息抵达Broker, Broker要将消息写入磁盘(持久化)才算成功。此时
             * Broker尚未持久化完成,宕机。
             * i： publisher也必须加入确认回调机制,确认成功的消息,修改数据库消息状态
             * 3。消费者收到消息,但没来得及消费然后宕机
             * i：定开启手动ACK,消费成功才移除,失败或者没来得及处理就noACk并重新入队
             *
             *
             */
            rabbitTemplate.convertAndSend("order-event-exchange","order.release.other",orderTo);

        }
    }
}