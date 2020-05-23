package com.qiangzengy.mall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.qiangzengy.common.constant.OrderConstant;
import com.qiangzengy.common.utils.R;
import com.qiangzengy.mall.order.entity.OrderItemEntity;
import com.qiangzengy.mall.order.feign.CartFeignService;
import com.qiangzengy.mall.order.feign.MemberFeignService;
import com.qiangzengy.mall.order.feign.ProductFeignService;
import com.qiangzengy.mall.order.feign.WmsFeignService;
import com.qiangzengy.mall.order.interceptor.LoginUserInterceptor;
import com.qiangzengy.mall.order.to.OrderCreateTo;
import com.qiangzengy.mall.order.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


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
    public OrderSubmitRespVo submitOrder(OrderSubmitVo orderVo) {
        submitVo.set(orderVo);
        OrderSubmitRespVo respVo=new OrderSubmitRespVo();
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        //下单：验证令牌->创建订单->校验价格->锁库存

        //验证令牌，原子性
        /**
         * 如果redis调用get方法来获取一个key的值，这get出来的值等于我们传过来的值，
         * 然后它会执行，把值删除，删除成功返回1，否则返回0
         */
        String script="if redis.call('get', KEYS[1])==ARGV[1]then return redis.call('del', KEYS[1])else return O end";
        String pageToken=orderVo.getOrderToken();
        Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResVo.getId()), pageToken);
        if (execute==1){
            //令牌验证成功
            OrderCreateTo order = createOrder();
        }
        return respVo;
    }

    /**
     * 创建订单
     */
    private OrderCreateTo createOrder(){

        OrderCreateTo createTo = new OrderCreateTo();

        String orderSn = IdWorker.getTimeId();

        OrderEntity entity = buildOrder(orderSn);

        //获取所有订单项信息
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

        return createTo;
    }

    private OrderEntity buildOrder(String orderSn) {
        OrderEntity entity=new OrderEntity();
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
        entity.setGiftGrowth(item.getPrice().intValue());
        entity.setGiftIntegration(item.getPrice().intValue());

        return entity;
    }
}