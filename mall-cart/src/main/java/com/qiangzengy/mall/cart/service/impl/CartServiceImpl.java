package com.qiangzengy.mall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.qiangzengy.common.constant.CartConstant;
import com.qiangzengy.common.utils.R;
import com.qiangzengy.mall.cart.feign.ProductFeignService;
import com.qiangzengy.mall.cart.interceptor.CartInterceptor;
import com.qiangzengy.mall.cart.service.CartService;
import com.qiangzengy.mall.cart.to.UserInfoTo;
import com.qiangzengy.mall.cart.vo.Cart;
import com.qiangzengy.mall.cart.vo.CartItem;
import com.qiangzengy.mall.cart.vo.SkuInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


/**
 * redis中购物车数据结构：
 * cart:1: 1:{skuId:1,title:"华为",price:2999,.....},7:{skuId:3,title:"华为",price:2999,.....}
 * Map<String k1,Map<String k2, CartItem item>>
 *     k1:标示每个用户的购物车
 *     k2:购物项的商品id
 *
 *     在redis中，key：用户标识
 *               value：Hash （k：商品id，v：购物项详情)
 */

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor executor;

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {

        //获取购物车数据
        BoundHashOperations<String, Object, Object>operations=getCartOps();
        //如果购物车有当前商品，只需要修改商品数量即可
        String str=(String) operations.get(skuId.toString());
        if (StringUtils.isEmpty(str)){
            CartItem cartItem=new CartItem();

            //远程查询当前要添加的商品信息
            CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
                //商品信息
                R r = productFeignService.info(skuId);
                SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                //将商品添加到购物车
                cartItem.setSkuId(skuInfo.getSkuId());
                cartItem.setCount(num);
                cartItem.setChec(true);
                cartItem.setIamge(skuInfo.getSkuDefaultImg());
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setPrice(skuInfo.getPrice());
            }, executor);

            CompletableFuture<Void> attrFuture = CompletableFuture.runAsync(() -> {
                //远程查询sku的组合信息
                List<String> stringList = productFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(stringList);
            }, executor);

            CompletableFuture.allOf(skuInfoFuture,attrFuture).get();
            //将商品加入购物车
            operations.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }else {
            //有该商品，修改商品数量
            CartItem cartItem= JSON.parseObject(str, CartItem.class);
            cartItem.setCount(cartItem.getCount()+num);
            operations.put(cartItem.getSkuId(), JSON.toJSONString(cartItem));
            return cartItem;
        }

    }


    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object>operations=getCartOps();
        String str=(String) operations.get(skuId.toString());
        return JSON.parseObject(str,CartItem.class);
    }


    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        // 获取用户信息，从ThreadLocal中获取
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        Cart cart=new Cart();
        if (userInfoTo.getUserId()!=null){
            //登陆
            String key=CartConstant.CART_PREFIX+userInfoTo.getUserId();
            //BoundHashOperations<String, Object, Object>operations=redisTemplate.boundHashOps(key);
            //如果临时购物车还有数据，需要合并
            //1。判断临时购物车是否有数据
            String empkey=CartConstant.CART_PREFIX+userInfoTo.getUserKey();
            List<CartItem> cartItems = getCartItems(empkey);
            if (cartItems!=null){
                //将临时购物车里面的购物项添加到在线购物车
                for (CartItem cartItem : cartItems) {
                    Long skuId = cartItem.getSkuId();
                    Integer count = cartItem.getCount();
                    addToCart(skuId,count);
                }
            }
            //合并后再来获取购物车数据
            List<CartItem> cartItem = getCartItems(key);
            cart.setItems(cartItem);
            //清除临时购物车的数据
            clearCart(empkey);
        }else {
            //没登陆
            String key=CartConstant.CART_PREFIX+userInfoTo.getUserKey();
            //获取临时购物车中所有购物项
            List<CartItem> cartItem = getCartItems(key);
            cart.setItems(cartItem);
        }
        return cart;
    }


    /**
     * 清空购物车
     * @param key
     */
    @Override
    public void clearCart(String key){
        redisTemplate.delete(key);
    }

    /**
     * 获取购物中所有购物项
     * @param key
     * @return
     */
    private List<CartItem> getCartItems( String key) {
        //获取购物车里面的商品信息
        //Object o = redisTemplate.opsForHash().get("cart", "skuId");
        //获取购物车信息
        BoundHashOperations<String, Object, Object> operations=redisTemplate.boundHashOps(key);
        List<Object> values = operations.values();
        if (values!=null&& values.size()>0){
            return values.stream().map(obj -> {
                String str = (String) obj;
                JSON.parseObject(str, CartItem.class);
                return new CartItem();
            }).collect(Collectors.toList());
        }
        return null;
    }


    /**
     * 获取redis中该用户的购物车信息
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        //得到用户信息
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        String cartKey;
        if(userInfoTo.getUserId()!=null){
            //登陆，redis的key
            cartKey= CartConstant.CART_PREFIX+userInfoTo.getUserId();
        }else {
            //没登陆，redis的key
            cartKey= CartConstant.CART_PREFIX+userInfoTo.getUserKey();
        }
        //取到操作的购物车
        return redisTemplate.boundHashOps(cartKey);
    }


    @Override
    public void checkItem(Long skuId, Integer check) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setChec(check == 1);
        BoundHashOperations<String, Object, Object> operations = getCartOps();
        operations.put(skuId.toString(),JSON.toJSONString(cartItem));

    }

    @Override
    public void countItem(Long skuId, Integer num) {

        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> operations = getCartOps();
        operations.put(skuId.toString(),JSON.toJSONString(cartItem));

    }

    @Override
    public List<CartItem> getCurrentCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo==null){
            return null;
        }

        String key=CartConstant.CART_PREFIX+userInfoTo.getUserId();

        //所有购物项
        List<CartItem> cartItems = getCartItems(key);

        //只需要选中的购物项
        assert cartItems != null;
        return cartItems.stream().filter(CartItem::getChec)
                .map(item -> {
                    //查询商品服务，商品的价格
                    R r = productFeignService.info(item.getSkuId());
                    SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {});
                    //更新为最新价格
                    item.setPrice(skuInfo.getPrice());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
